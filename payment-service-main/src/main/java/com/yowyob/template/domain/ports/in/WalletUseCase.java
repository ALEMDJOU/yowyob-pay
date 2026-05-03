package com.yowyob.template.domain.ports.in;

import com.yowyob.template.domain.model.Wallet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Cas d’usage pour la gestion du cycle de vie des portefeuilles.
 */
public interface WalletUseCase {

    /**
     * Crée un nouveau portefeuille.
     *
     * @param wallet données du portefeuille (souvent sans id, assigné côté stockage)
     * @return le portefeuille persisté
     */
    Mono<Wallet> createWallet(Wallet wallet);

    /**
     * Récupère le portefeuille d’un propriétaire (un seul attendu par propriétaire).
     *
     * @param ownerId identifiant du propriétaire
     * @return le portefeuille ou une erreur {@code NotFound} si absent
     */
    Mono<Wallet> getWalletByOwnerId(UUID ownerId);

    /**
     * Met à jour un portefeuille existant.
     *
     * @param wallet entité complète à enregistrer
     * @return le portefeuille mis à jour
     */
    Mono<Wallet> updateWallet(Wallet wallet);

    /**
     * Supprime définitivement un portefeuille par identifiant.
     *
     * @param id identifiant du portefeuille
     * @return complétion réactive quand la suppression est faite
     */
    Mono<Void> deleteWallet(UUID id);

    /**
     * Récupère un portefeuille par identifiant.
     *
     * @param id identifiant unique
     * @return le portefeuille ou une erreur {@code NotFound} si absent
     */
    Mono<Wallet> getWalletById(UUID id);

    /**
     * Retourne tous les portefeuilles connus.
     *
     * @return flux (peut être vide)
     */
    Flux<Wallet> getAllWallets();
}
