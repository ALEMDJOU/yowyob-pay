package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Corps de demande de recharge vers le payment-service.
 *
 * @param targetWalletId identifiant du portefeuille à créditer
 * @param amount         montant positif attendu
 */
public record RechargeRequest(
                @NotNull(message = "targetWalletId est obligatoire") UUID targetWalletId,
                @NotNull(message = "amount est obligatoire") @Positive(message = "amount doit être strictement positif") BigDecimal amount) {
}
