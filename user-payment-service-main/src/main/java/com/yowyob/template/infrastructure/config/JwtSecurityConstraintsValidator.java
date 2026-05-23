package com.yowyob.template.infrastructure.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

/**
 * Nom : {@code JwtSecurityConstraintsValidator}
 * <p>
 * Description : valide au démarrage les contraintes sur la durée de vie des JWT
 * en profil
 * {@code prod} (expiration obligatoire et plafonnée pour limiter l’exposition
 * en cas de fuite).
 * </p>
 */
@Component
@Profile("prod")
public class JwtSecurityConstraintsValidator {

    private static final long MAX_EXPIRATION_MS = 86_400_000L;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Nom : {@code validateJwtSettings}
     * <p>
     * Description : impose une expiration JWT strictement positive et inférieure ou
     * égale à 24 h.
     * </p>
     *
     * @throws IllegalStateException si la configuration est invalide pour la
     *                               production
     */
    @PostConstruct
    public void validateJwtSettings() {
        if (jwtExpirationMs <= 0) {
            throw new IllegalStateException(
                    "application.security.jwt.expiration doit être > 0 en profil prod");
        }
        if (jwtExpirationMs > MAX_EXPIRATION_MS) {
            throw new IllegalStateException(
                    "application.security.jwt.expiration ne doit pas dépasser 86400000 ms (24h) en profil prod");
        }
    }
}
