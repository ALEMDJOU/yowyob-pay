package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Corps de demande de recharge vers le payment-service.
 *
 * @param targetWalletId identifiant du portefeuille à créditer
 * @param amount         montant positif attendu
 */
public record RechargeRequest(UUID targetWalletId, BigDecimal amount) {}
