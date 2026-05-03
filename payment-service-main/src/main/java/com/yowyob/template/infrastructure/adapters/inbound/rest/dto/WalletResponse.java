package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Réponse REST exposant l’état complet d’un portefeuille.
 *
 * @param id        identifiant technique
 * @param ownerId   propriétaire
 * @param ownerName nom
 * @param balance   solde courant
 */
public record WalletResponse(UUID id, UUID ownerId, String ownerName, BigDecimal balance) {
}
