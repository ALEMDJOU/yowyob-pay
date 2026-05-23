package com.yowyob.template.infrastructure.config;

import java.util.Base64;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.yowyob.template.infrastructure.security.InternalApiKeyWebFilter;

/**
 * Nom : {@code SecurityConfig}
 * <p>
 * Description : définit deux chaînes de filtres WebFlux ordonnées : (1) API
 * métier portefeuilles
 * et transactions protégées par clé API interne ; (2) le reste (Swagger
 * conditionnel, Actuator,
 * tout autre chemin) avec validation JWT OAuth2 resource server.
 * </p>
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    @Value("${application.security.jwt.secret}")
    private String secretKey;

    /**
     * Nom : constructeur
     * <p>
     * Description : injecte les propriétés de sécurité pour CORS et la
     * configuration des chaînes.
     * </p>
     *
     * @param securityProperties paramètres {@code application.security.*}
     */
    public SecurityConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Nom : {@code internalApiSecurityFilterChain}
     * <p>
     * Description : chaîne prioritaire pour {@code /api/v1/wallets/**} et
     * {@code /api/v1/transactions/**}
     * avec authentification par en-tête
     * {@link InternalApiKeyWebFilter#HEADER_NAME}.
     * </p>
     *
     * @param http builder WebFlux Security
     * @return chaîne compilée
     */
    @Bean
    @org.springframework.core.annotation.Order(0)
    public SecurityWebFilterChain internalApiSecurityFilterChain(ServerHttpSecurity http) {
        InternalApiKeyWebFilter internalApiKeyWebFilter = new InternalApiKeyWebFilter(securityProperties);
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/api/v1/wallets/**",
                        "/api/v1/transactions/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
                .addFilterBefore(internalApiKeyWebFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                .build();
    }

    /**
     * Nom : {@code defaultSecurityFilterChain}
     * <p>
     * Description : chaîne par défaut (ordre supérieur) pour Swagger, Actuator et
     * chemins
     * restants avec JWT Bearer.
     * </p>
     *
     * @param http builder WebFlux Security
     * @return chaîne compilée
     */
    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui.html").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder())))
                .build();
    }

    /**
     * Nom : {@code corsConfigurationSource}
     * <p>
     * Description : construit la politique CORS à partir de
     * {@link SecurityProperties#allowedOriginsList()}.
     * </p>
     *
     * @return source CORS enregistrée sur {@code /**}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = securityProperties.allowedOriginsList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Nom : {@code jwtDecoder}
     * <p>
     * Description : décodeur JWT HMAC-SHA256 aligné sur la clé Base64
     * {@code application.security.jwt.secret}.
     * </p>
     *
     * @return décodeur réactif OAuth2
     * @throws IllegalArgumentException si le secret est vide ou non décodable en
     *                                  Base64
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException(
                    "application.security.jwt.secret (JWT_SECRET) doit être défini (clé Base64, ex. openssl rand -base64 32)");
        }
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKeySpec).build();
    }
}
