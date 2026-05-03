package com.yowyob.template.domain.ports.out;

import com.yowyob.template.domain.model.Wallet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port de persistance des portefeuilles.
 */
public interface WalletRepositoryPort {

    /**
     * @param id identifiant du portefeuille
     * @return le portefeuille ou {@link Mono#empty()} si absent
     */
    Mono<Wallet> findById(UUID id);

    /**
     * @param wallet entité à insérer ou remplacer
     * @return l’enregistrement persistant
     */
    Mono<Wallet> save(Wallet wallet);

    /**
     * @param ownerId identifiant du propriétaire
     * @return le portefeuille associé ou {@link Mono#empty()}
     */
    Mono<Wallet> findByOwnerId(UUID ownerId);

    /**
     * @return tous les portefeuilles
     */
    Flux<Wallet> findAllWallets();

    /**
     * @param id portefeuille à supprimer
     * @return complétion quand l’opération est terminée
     */
    Mono<Void> deleteById(UUID id);

    /**
     * Mise à jour ciblée du portefeuille.
     *
     * @param wallet entité complète
     * @return le portefeuille après mise à jour
     */
    Mono<Wallet> updateWallet(Wallet wallet);
}
