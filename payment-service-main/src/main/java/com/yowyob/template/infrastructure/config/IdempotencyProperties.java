package com.yowyob.template.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paramètres {@code application.idempotency.*} (rétention documentée pour purge
 * manuelle ou jobs futurs).
 *
 * @param retention durée indicative de conservation des lignes
 *                  (informationnelle
 *                  tant qu’aucun job de purge n’est branché)
 */
@ConfigurationProperties(prefix = "application.idempotency")
public record IdempotencyProperties(Duration retention) {
}
