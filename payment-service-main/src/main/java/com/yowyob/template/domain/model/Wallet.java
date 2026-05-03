package com.yowyob.template.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Portefeuille numérique du domaine (solde et propriétaire).
 *
 * @param id         identifiant unique du portefeuille
 * @param ownerId    identifiant du propriétaire
 * @param ownerName  libellé du propriétaire
 * @param balance    solde courant
 */
public record Wallet(UUID id, UUID ownerId, String ownerName, BigDecimal balance) {
    /**
     * Retourne une copie du portefeuille avec un solde modifié.
     *
     * @param newBalance nouveau solde
     * @return un {@link Wallet} immuable avec le solde mis à jour
     */
    public Wallet withBalance(BigDecimal newBalance) {
        return new Wallet(id, ownerId, ownerName, newBalance);
    }
}
