package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import com.yowyob.template.domain.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Requête de création d’opération sur portefeuille (montant strictement positif).
 *
 * @param walletId portefeuille cible
 * @param amount   montant métier
 * @param type     nature RECHARGE ou PAYMENT
 */
public record TransactionRequest(@NotNull UUID walletId, @Positive BigDecimal amount, TransactionType type) {
}
