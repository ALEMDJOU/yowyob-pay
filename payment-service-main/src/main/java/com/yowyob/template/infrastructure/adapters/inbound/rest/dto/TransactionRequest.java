package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.yowyob.template.domain.model.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Requête de création d’opération sur portefeuille (montant strictement
 * positif).
 *
 * @param walletId portefeuille cible
 * @param amount   montant métier
 * @param type     nature RECHARGE ou PAYMENT
 */
public record TransactionRequest(
                @NotNull(message = "walletId est obligatoire") UUID walletId,
                @NotNull(message = "amount est obligatoire") @Positive(message = "amount doit être strictement positif") BigDecimal amount,
                @NotNull(message = "type est obligatoire") TransactionType type) {
}
