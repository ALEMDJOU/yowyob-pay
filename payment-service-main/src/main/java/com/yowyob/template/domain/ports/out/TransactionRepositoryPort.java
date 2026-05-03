package com.yowyob.template.domain.ports.out;

import com.yowyob.template.domain.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port de persistance des transactions (implémentation typique : R2DBC / PostgreSQL).
 */
public interface TransactionRepositoryPort {

    /**
     * Enregistre ou met à jour une transaction.
     *
     * @param transaction entité du domaine
     * @return l’enregistrement sauvegardé
     */
    Mono<Transaction> save(Transaction transaction);

    /**
     * Recherche par clé primaire.
     *
     * @param id identifiant
     * @return la transaction ou {@link Mono#empty()} si inconnue
     */
    Mono<Transaction> getTransactionById(UUID id);

    /**
     * Filtre par portefeuille cible.
     *
     * @param walletId identifiant de portefeuille
     * @return toutes les transactions liées
     */
    Flux<Transaction> getTransactionsByWalletId(UUID walletId);
}
