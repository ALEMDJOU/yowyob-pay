package com.yowyob.template.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Nom : {@code UserBusinessProperties}
 * <p>
 * Description : plafonds métier pour les recharges publiées vers Kafka.
 * </p>
 *
 * @param maxRechargeAmount montant maximal autorisé pour une demande de
 *                          recharge
 */
@ConfigurationProperties(prefix = "application.business")
public record UserBusinessProperties(BigDecimal maxRechargeAmount) {

    /**
     * Plafond par défaut si non configuré.
     */
    public UserBusinessProperties {
        if (maxRechargeAmount == null) {
            maxRechargeAmount = new BigDecimal("1000000");
        }
    }
}
