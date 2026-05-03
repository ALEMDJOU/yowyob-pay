package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import com.yowyob.template.domain.model.TransactionStatus;
import com.yowyob.template.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Vue sérialisée d’une transaction persistée.
 *
 * @param id       identifiant
 * @param walletId portefeuille lié
 * @param amount   montant enregistré
 * @param type     type métier
 * @param status   état final ou intermédiaire
 */
public record TransactionResponse(UUID id, UUID walletId, BigDecimal amount, TransactionType type, TransactionStatus status) {
}
