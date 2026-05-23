package com.yowyob.template.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Nom : {@code UserPaymentSecurityProperties}
 * <p>
 * Description : paramètres de sécurité propres au microservice user-payment
 * (CORS,
 * exposition de la documentation OpenAPI, sans clé API interne payment).
 * </p>
 *
 * @param exposeOpenapi         si {@code false}, désactiver springdoc via YAML
 *                              de profil
 * @param corsAllowedOriginsCsv origines CORS séparées par des virgules
 */
@ConfigurationProperties(prefix = "application.security")
public record UserPaymentSecurityProperties(
        Boolean exposeOpenapi,
        String corsAllowedOriginsCsv) {

    /**
     * Valeurs par défaut après liaison Spring.
     */
    public UserPaymentSecurityProperties {
        if (exposeOpenapi == null) {
            exposeOpenapi = true;
        }
        if (corsAllowedOriginsCsv == null || corsAllowedOriginsCsv.isBlank()) {
            corsAllowedOriginsCsv = "http://localhost:3000,http://localhost:3999";
        }
    }

    /**
     * @return {@code true} si Swagger / OpenAPI doivent être exposés
     */
    public boolean isExposeOpenapi() {
        return Boolean.TRUE.equals(exposeOpenapi);
    }

    /**
     * Nom : {@code allowedOriginsList}
     * <p>
     * Description : découpe la liste CSV des origines CORS.
     * </p>
     *
     * @return origines autorisées
     */
    public List<String> allowedOriginsList() {
        return Arrays.stream(corsAllowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
