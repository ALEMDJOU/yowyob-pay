package com.yowyob.template.domain.model;

import java.util.UUID;

/**
 * Compte agent avec statut et mot de passe (haché en persistance).
 *
 * @param id       identifiant
 * @param name     nom affiché
 * @param email    adresse unique
 * @param password mot de passe (haché) - peut être masqué côté API
 * @param status   libellé de statut (ex. ACTIVE)
 */
public record Agent(UUID id, String name, String email, String password, String status) {

    /**
     * Copie de l’agent sans exposer le secret.
     *
     * @return même agent avec {@code password} à {@code null}
     */
    public Agent withoutPassword() {
        return new Agent(id, name, email, null, status);
    }
}
