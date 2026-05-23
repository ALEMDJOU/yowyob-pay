package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.application.service.IdempotencyService;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.WalletResponse;
import com.yowyob.template.infrastructure.config.BusinessProperties;
import com.yowyob.template.infrastructure.config.SecurityConfig;
import com.yowyob.template.infrastructure.config.SecurityProperties;
import com.yowyob.template.infrastructure.mappers.WalletMapper;
import com.yowyob.template.infrastructure.security.InternalApiKeyWebFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests WebFlux : pagination obligatoire sur {@code GET /api/v1/wallets}.
 */
@WebFluxTest(controllers = WalletController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class, ValidationAutoConfiguration.class })
@EnableConfigurationProperties({ SecurityProperties.class, BusinessProperties.class })
@TestPropertySource(properties = {
        "application.security.jwt.secret=B7lNty52nURS1lCg6KjEvPh6e71c/ndOh1H4mCMRMgo=",
        "application.security.internal-api-key=test-internal-api-key-for-security-tests",
        "application.security.cors-allowed-origins-csv=http://localhost:3000",
        "application.business.max-transaction-amount=999999",
        "application.security.expose-openapi=true"
})
class WalletControllerPaginationWebFluxTest {

    private static final String API_KEY = "test-internal-api-key-for-security-tests";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WalletUseCase useCase;

    @MockBean
    private WalletMapper mapper;

    @MockBean
    private IdempotencyService idempotencyService;

    @BeforeEach
    void setupMapper() {
        when(mapper.toResponse(any(Wallet.class)))
                .thenAnswer(invocation -> {
                    Wallet w = invocation.getArgument(0);
                    return new WalletResponse(w.id(), w.ownerId(), w.ownerName(), w.balance());
                });
    }

    @Test
    @DisplayName("GET /wallets sans page → 400")
    void missingPage_returnsBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/wallets").queryParam("size", "20").build())
                .header(InternalApiKeyWebFilter.HEADER_NAME, API_KEY)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET /wallets size > 50 → 400")
    void sizeAboveMax_returnsBadRequest() {
        webTestClient.get()
                .uri("/api/v1/wallets?page=0&size=51")
                .header(InternalApiKeyWebFilter.HEADER_NAME, API_KEY)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET /wallets pagination valide → 200 et corps paginé")
    void validPagination_returnsPagedBody() {
        UUID wid = UUID.randomUUID();
        UUID oid = UUID.randomUUID();
        Wallet wallet = new Wallet(wid, oid, "owner", BigDecimal.TEN);
        when(useCase.getWalletsPage(0, 20))
                .thenReturn(Mono.just(new WalletPage(List.of(wallet), 0, 20, 1L, 1)));

        webTestClient.get()
                .uri("/api/v1/wallets?page=0&size=20")
                .header(InternalApiKeyWebFilter.HEADER_NAME, API_KEY)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.totalPages").isEqualTo(1)
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(20)
                .jsonPath("$.content[0].id").isEqualTo(wid.toString());
    }
}
