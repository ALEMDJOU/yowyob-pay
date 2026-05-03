package com.yowyob.template.infrastructure.adapters.inbound.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Événement métier : demande de prélever une commission proportionnelle à {@code baseAmount} pour un propriétaire.
 *
 * @param ownerId    propriétaire concerné
 * @param baseAmount montant de référence pour le calcul du pourcentage
 */
public record PaymentCommissionEvent(UUID ownerId, BigDecimal baseAmount) {
}
