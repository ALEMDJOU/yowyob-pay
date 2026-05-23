package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Corps de création / mise à jour de portefeuille côté API (sans solde : géré
 * côté domaine).
 *
 * @param ownerId   identifiant du propriétaire
 * @param ownerName nom affiché
 */
public record WalletRequest(
                @NotNull(message = "ownerId est obligatoire") UUID ownerId,
                @NotBlank(message = "ownerName est obligatoire") @Size(max = 120, message = "ownerName trop long") String ownerName) {
}
