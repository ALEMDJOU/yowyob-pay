package com.yowyob.template.infrastructure.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nom : {@code PaymentProperties}
 * <p>
 * Description : paramètres métier du flux paiement (taux de commission appliqué
 * au
 * montant de référence d’une transaction {@code PAYMENT}), chargés depuis
 * {@code application.payment.*}.
 * </p>
 *
 * @param commissionRate part entre 0 et 1 (ex. 0.1 pour 10 %) utilisée pour
 *                       calculer la commission débitée du solde
 */
@ConfigurationProperties(prefix = "application.payment")
public record PaymentProperties(BigDecimal commissionRate) {

    /**
     * Valide le taux et applique une valeur par défaut si la propriété est absente.
     */
    public PaymentProperties {
        BigDecimal rate = commissionRate != null ? commissionRate : new BigDecimal("0.1");
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "application.payment.commission-rate doit être compris entre 0 et 1 inclus");
        }
        commissionRate = rate;
    }
}
