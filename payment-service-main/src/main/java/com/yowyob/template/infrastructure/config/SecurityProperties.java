package com.yowyob.template.infrastructure.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nom : {@code SecurityProperties}
 * <p>
 * Description : propriétés de sécurité applicative (clé API interne, exposition
 * OpenAPI,
 * liste d’origines CORS) chargées depuis {@code application.security.*}.
 * </p>
 *
 * @param internalApiKey        valeur attendue pour l’en-tête
 *                              {@code X-Internal-Api-Key} sur les API métier
 * @param exposeOpenapi         si {@code false}, la documentation OpenAPI /
 *                              Swagger doit être désactivée côté YAML
 * @param corsAllowedOriginsCsv origines CORS séparées par des virgules (une
 *                              seule propriété chaîne pour faciliter les
 *                              variables d’environnement)
 */
@ConfigurationProperties(prefix = "application.security")
public record SecurityProperties(
        String internalApiKey,
        Boolean exposeOpenapi,
        String corsAllowedOriginsCsv) {

    /**
     * Normalise les valeurs nulles après liaison Spring.
     */
    public SecurityProperties {
        if (internalApiKey == null) {
            internalApiKey = "";
        }
        if (exposeOpenapi == null) {
            exposeOpenapi = true;
        }
        if (corsAllowedOriginsCsv == null || corsAllowedOriginsCsv.isBlank()) {
            corsAllowedOriginsCsv = "http://localhost:3000,http://localhost:3999";
        }
    }

    /**
     * Nom : {@code allowedOriginsList}
     * <p>
     * Description : découpe {@link #corsAllowedOriginsCsv} en liste d’origines non
     * vides.
     * </p>
     *
     * @return liste des origines autorisées pour CORS
     */
    public List<String> allowedOriginsList() {
        if (corsAllowedOriginsCsv == null || corsAllowedOriginsCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(corsAllowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
