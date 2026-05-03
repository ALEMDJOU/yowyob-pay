package com.yowyob.template.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Opération financière enregistrée (crédit ou débit) sur un portefeuille.
 *
 * @param id       identifiant unique de la transaction
 * @param walletId portefeuille concerné
 * @param amount   montant (positif selon les règles métier du handler)
 * @param type     nature : paiement ou recharge
 * @param status   état du traitement
 */
public record Transaction(UUID id, UUID walletId, BigDecimal amount, TransactionType type, TransactionStatus status) {}

