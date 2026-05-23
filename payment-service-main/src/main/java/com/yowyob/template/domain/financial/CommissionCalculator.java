package com.yowyob.template.domain.financial;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilitaire de domaine : calcul homogène de la commission sur un montant de
 * référence (même règle que
 * {@link com.yowyob.template.domain.handler.PaymentHandler}).
 */
public final class CommissionCalculator {

    private CommissionCalculator() {
    }

    /**
     * Nom : {@code commissionFromBaseAmount}
     * <p>
     * Description : applique le taux {@code rate} au {@code baseAmount} avec
     * arrondi {@link RoundingMode#HALF_UP} sur 4 décimales (cohérence API REST et
     * événements Kafka).
     * </p>
     *
     * @param baseAmount montant de référence (non nul)
     * @param rate       taux entre 0 et 1 (ex. 0,1 pour 10 %)
     * @return commission arrondie à 4 décimales
     * @throws NullPointerException si un argument est {@code null}
     */
    public static BigDecimal commissionFromBaseAmount(BigDecimal baseAmount, BigDecimal rate) {
        return baseAmount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
    }
}
