package com.yowyob.template.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.yowyob.template.infrastructure.config.SecurityProperties;

import reactor.core.publisher.Mono;

/**
 * Nom : {@code InternalApiKeyWebFilter}
 * <p>
 * Description : filtre WebFlux placé avant l’autorisation sur la chaîne dédiée
 * aux chemins
 * {@code /api/v1/wallets/**} et {@code /api/v1/transactions/**}. Compare
 * l’en-tête
 * {@value #HEADER_NAME} à la clé configurée avec une comparaison sur empreinte
 * SHA-256
 * pour limiter les fuites par timing sur la clé brute.
 * </p>
 */
public class InternalApiKeyWebFilter implements WebFilter {

    /**
     * Nom de l’en-tête HTTP attendu pour la clé API interne (backends uniquement).
     */
    public static final String HEADER_NAME = "X-Internal-Api-Key";

    private static final String INTERNAL_PRINCIPAL = "internal-service";

    private final SecurityProperties securityProperties;

    /**
     * Nom : constructeur
     * <p>
     * Description : injecte la configuration de sécurité contenant la clé attendue.
     * </p>
     *
     * @param securityProperties propriétés {@code application.security.*}
     */
    public InternalApiKeyWebFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Nom : {@code filter}
     * <p>
     * Description : valide la clé API ou coupe la chaîne avec un statut HTTP
     * d’erreur,
     * puis propage un contexte de sécurité réactif avec le rôle
     * {@code ROLE_INTERNAL_CLIENT}.
     * </p>
     *
     * @param exchange contexte d’échange HTTP réactif
     * @param chain    chaîne de filtres suivante
     * @return mono complété lorsque la réponse est envoyée ou la chaîne poursuivie
     */
    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String configured = securityProperties.internalApiKey();
        ServerHttpResponse response = exchange.getResponse();
        if (configured == null || configured.isBlank()) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return response.setComplete();
        }
        String presented = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        if (!sha256ConstantTimeEquals(presented, configured)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                INTERNAL_PRINCIPAL,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_CLIENT")));
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    /**
     * Nom : {@code sha256ConstantTimeEquals}
     * <p>
     * Description : compare deux chaînes en passant par SHA-256 pour obtenir des
     * tableaux
     * de même longueur avant {@link MessageDigest#isEqual(byte[], byte[])}.
     * </p>
     *
     * @param a première chaîne (peut être {@code null})
     * @param b seconde chaîne (peut être {@code null})
     * @return {@code true} si les deux empreintes SHA-256 sont identiques
     */
    static boolean sha256ConstantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] da = md.digest(a.getBytes(StandardCharsets.UTF_8));
            byte[] db = md.digest(b.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(da, db);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }
}
