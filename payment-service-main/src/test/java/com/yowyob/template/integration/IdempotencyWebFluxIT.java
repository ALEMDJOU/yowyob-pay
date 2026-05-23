package com.yowyob.template.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.template.infrastructure.security.InternalApiKeyWebFilter;

/**
 * Idempotence HTTP sur wallets et transactions : rejouer la même requête
 * renvoie
 * le même corps ; corps différent → 409.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class IdempotencyWebFluxIT {

        private static final String INTERNAL_KEY = "it-internal-api-key";

        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

        @DynamicPropertySource
        static void registerDatasource(DynamicPropertyRegistry registry) {
                registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://%s:%d/%s".formatted(
                                postgres.getHost(),
                                postgres.getMappedPort(5432),
                                postgres.getDatabaseName()));
                registry.add("spring.r2dbc.username", postgres::getUsername);
                registry.add("spring.r2dbc.password", postgres::getPassword);
                registry.add("spring.liquibase.url", postgres::getJdbcUrl);
                registry.add("spring.liquibase.user", postgres::getUsername);
                registry.add("spring.liquibase.password", postgres::getPassword);
                registry.add("spring.liquibase.enabled", () -> "true");
                registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.xml");
        }

        @Autowired
        private WebTestClient webTestClient;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void walletCreateReplayReturnsSameBody() {
                UUID ownerId = UUID.randomUUID();
                String key = "idem-wallet-" + ownerId;
                Map<String, Object> body = Map.of(
                                "ownerId", ownerId.toString(),
                                "ownerName", "Dupont");

                String first = webTestClient.post()
                                .uri("/api/v1/wallets")
                                .header(InternalApiKeyWebFilter.HEADER_NAME, INTERNAL_KEY)
                                .header("Idempotency-Key", key)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();

                assertThat(first).isNotBlank();

                String second = webTestClient.post()
                                .uri("/api/v1/wallets")
                                .header(InternalApiKeyWebFilter.HEADER_NAME, INTERNAL_KEY)
                                .header("Idempotency-Key", key)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();

                assertThat(second).isEqualTo(first);

                Map<String, Object> conflictingBody = Map.of(
                                "ownerId", ownerId.toString(),
                                "ownerName", "Martin");

                webTestClient.post()
                                .uri("/api/v1/wallets")
                                .header(InternalApiKeyWebFilter.HEADER_NAME, INTERNAL_KEY)
                                .header("Idempotency-Key", key)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(conflictingBody)
                                .exchange()
                                .expectStatus().isEqualTo(409);
        }

        @Test
        void transactionRechargeReplayReturnsSameBody() throws Exception {
                UUID ownerId = UUID.randomUUID();
                String wKey = "idem-pre-wallet-" + ownerId;
                Map<String, Object> walletBody = Map.of(
                                "ownerId", ownerId.toString(),
                                "ownerName", "Agent");

                String walletJson = webTestClient.post()
                                .uri("/api/v1/wallets")
                                .header(InternalApiKeyWebFilter.HEADER_NAME, INTERNAL_KEY)
                                .header("Idempotency-Key", wKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(walletBody)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();

                assertThat(walletJson).contains("\"balance\"");
                JsonNode root = objectMapper.readTree(walletJson);
                UUID walletId = UUID.fromString(root.get("id").asText());

                String tKey = "idem-tx-" + ownerId;
                Map<String, Object> txBody = Map.of(
                                "walletId", walletId.toString(),
                                "amount", 5,
                                "type", "RECHARGE");

                String firstTx = webTestClient.post()
                                .uri("/api/v1/transactions")
                                .header(InternalApiKeyWebFilter.HEADER_NAME, INTERNAL_KEY)
                                .header("Idempotency-Key", tKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(txBody)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();

                String secondTx = webTestClient.post()
                                .uri("/api/v1/transactions")
                                .header(InternalApiKeyWebFilter.HEADER_NAME, INTERNAL_KEY)
                                .header("Idempotency-Key", tKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(txBody)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();

                assertThat(secondTx).isEqualTo(firstTx);

                Map<String, Object> conflictingTx = Map.of(
                                "walletId", walletId.toString(),
                                "amount", 7,
                                "type", "RECHARGE");

                webTestClient.post()
                                .uri("/api/v1/transactions")
                                .header(InternalApiKeyWebFilter.HEADER_NAME, INTERNAL_KEY)
                                .header("Idempotency-Key", tKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(conflictingTx)
                                .exchange()
                                .expectStatus().isEqualTo(409);
        }
}
