package com.yowyob.template.infrastructure.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nom : {@code BusinessProperties}
 * <p>
 * Description : plafonds métier configurables (montants max) pour les
 * transactions et,
 * côté documentation, alignement avec les validations côté services.
 * </p>
 *
 * @param maxTransactionAmount plafond absolu pour une opération de transaction
 *                             (paiement / recharge via API)
 */
@ConfigurationProperties(prefix = "application.business")
public record BusinessProperties(BigDecimal maxTransactionAmount) {

	/**
	 * Applique un plafond par défaut si la liaison Spring n’a pas fourni de valeur.
	 */
	public BusinessProperties {
		if (maxTransactionAmount == null) {
			maxTransactionAmount = new BigDecimal("1000000");
		}
	}
}
