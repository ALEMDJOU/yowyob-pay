package com.yowyob.template.domain.ports.in;

import java.util.UUID;

import com.yowyob.template.domain.model.Transaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Cas d’usage exposés pour la création et la consultation des transactions.
 */
public interface TransactionUseCase {

    /**
     * Crée et traite une transaction (paiement ou recharge selon le type).
     *
     * @param transaction modèle à persister ; l’identifiant peut être généré en couche infra
     * @return la transaction après traitement et persistance
     */
    Mono<Transaction> createTransaction(Transaction transaction);

    /**
     * Retrouve une transaction par son identifiant.
     *
     * @param id identifiant unique
     * @return la transaction ou vide si absente
     */
    Mono<Transaction> getTransactionById(UUID id);

    /**
     * Liste les transactions associées à un portefeuille.
     *
     * @param walletId identifiant du portefeuille
     * @return flux ordonné ou vide selon la persistance
     */
    Flux<Transaction> getTransactionsByWalletId(UUID walletId);
}
