package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import java.util.UUID;

/**
 * Corps de création / mise à jour de portefeuille côté API (sans solde : géré côté domaine).
 *
 * @param ownerId   identifiant du propriétaire
 * @param ownerName nom affiché
 */
public record WalletRequest (UUID ownerId, String ownerName) {}
