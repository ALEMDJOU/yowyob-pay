package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.application.service.IdempotencyService;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.WalletRequest;
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
 * Nom : {@code WalletControllerSecurityWebFluxTest}
 * <p>
 * Description : vérifie que les endpoints portefeuilles exigent l’en-tête
 * {@link InternalApiKeyWebFilter#HEADER_NAME} conformément à la chaîne de
 * sécurité interne.
 * </p>
 */
@WebFluxTest(controllers = WalletController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({ SecurityProperties.class, BusinessProperties.class })
@TestPropertySource(properties = {
        "application.security.jwt.secret=B7lNty52nURS1lCg6KjEvPh6e71c/ndOh1H4mCMRMgo=",
        "application.security.internal-api-key=test-internal-api-key-for-security-tests",
        "application.security.cors-allowed-origins-csv=http://localhost:3000",
        "application.business.max-transaction-amount=999999",
        "application.security.expose-openapi=true"
})
class WalletControllerSecurityWebFluxTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WalletUseCase useCase;

    @MockBean
    private WalletMapper mapper;

    @MockBean
    private IdempotencyService idempotencyService;

    /**
     * Nom : {@code setupMocks}
     * <p>
     * Description : configure des réponses factices pour le mapper et le cas
     * d’usage afin d’isoler la couche sécurité.
     * </p>
     */
    @BeforeEach
    void setupMocks() {
        when(mapper.toDomain(any(WalletRequest.class)))
                .thenReturn(new Wallet(null, UUID.randomUUID(), "owner", BigDecimal.ZERO));
        when(mapper.toResponse(any(Wallet.class)))
                .thenReturn(new WalletResponse(UUID.randomUUID(), UUID.randomUUID(), "owner", BigDecimal.TEN));
        when(useCase.getWalletsPage(0, 20)).thenReturn(Mono.just(new WalletPage(List.of(), 0, 20, 0L, 0)));
    }

    /**
     * Nom : {@code wallets_withoutApiKey_return401}
     * <p>
     * Description : sans clé interne, la réponse doit être 401 Unauthorized.
     * </p>
     */
    @Test
    @DisplayName("GET /api/v1/wallets sans en-tête interne → 401")
    void wallets_withoutApiKey_return401() {
        webTestClient.get().uri("/api/v1/wallets?page=0&size=20").exchange().expectStatus().isUnauthorized();
    }

    /**
     * Nom : {@code wallets_withWrongApiKey_return401}
     * <p>
     * Description : une clé incorrecte ne doit pas authentifier la requête.
     * </p>
     */
    @Test
    @DisplayName("GET /api/v1/wallets avec mauvaise clé → 401")
    void wallets_withWrongApiKey_return401() {
        webTestClient.get()
                .uri("/api/v1/wallets?page=0&size=20")
                .header(InternalApiKeyWebFilter.HEADER_NAME, "wrong-key")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Nom : {@code wallets_withValidApiKey_return200}
     * <p>
     * Description : la clé attendue permet d’atteindre le contrôleur (200 avec
     * corps vide ici).
     * </p>
     */
    @Test
    @DisplayName("GET /api/v1/wallets avec clé valide → 200")
    void wallets_withValidApiKey_return200() {
        webTestClient.get()
                .uri("/api/v1/wallets?page=0&size=20")
                .header(InternalApiKeyWebFilter.HEADER_NAME, "test-internal-api-key-for-security-tests")
                .exchange()
                .expectStatus().isOk();
    }
}
