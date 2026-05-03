package com.yowyob.template.domain.handler;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionStatus;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Orchestration réactive type « template method » pour une transaction : validation,
 * mise à jour du solde, persistance de l’historique.
 */
@RequiredArgsConstructor
public abstract class AbstractTransactionHandler {

    protected final WalletRepositoryPort walletRepository;
    protected final TransactionRepositoryPort transactionRepository;

    /**
     * Indique le type de transaction géré par cette implémentation.
     *
     * @return {@link TransactionType} associé au handler
     */
    public abstract TransactionType getTransactionType();

    /**
     * Chaîne complète : chargement du portefeuille, validation, calcul du solde,
     * mise à jour, création et sauvegarde de la transaction.
     *
     * @param walletId identifiant du portefeuille cible
     * @param amount     montant métier (interprété selon le concrete handler)
     * @return la transaction persistée à l’état {@link TransactionStatus#COMPLETED}
     * @throws RuntimeException via {@link reactor.core.publisher.Mono#error(Throwable)} si portefeuille absent ou règle métier violée
     */
    public Mono<Transaction> process(UUID walletId, BigDecimal amount) {
        TransactionType type = getTransactionType();
        return walletRepository.findById(walletId)
                .switchIfEmpty(Mono.error(new RuntimeException("Wallet not found")))
                .flatMap(wallet -> validate(wallet, amount))
                .flatMap(wallet -> applyBalance(wallet, amount))
                .flatMap(walletRepository::updateWallet)
                .flatMap(savedWallet -> createTransaction(savedWallet, amount, type))
                .flatMap(transactionRepository::save)
                .doOnSuccess(this::publishEvent);
    }

    /**
     * Contrôles métier avant modification du solde (montants, plafonds, etc.).
     *
     * @param wallet  portefeuille courant
     * @param amount  montant demandé
     * @return le portefeuille validé ou une erreur réactive
     */
    protected abstract Mono<Wallet> validate(Wallet wallet, BigDecimal amount);

    /**
     * Applique la variation de solde (débit ou crédit selon le handler).
     *
     * @param wallet  portefeuille de départ
     * @param amount  montant de l’opération
     * @return portefeuille avec le nouveau solde (non encore persisté)
     */
    protected abstract Mono<Wallet> applyBalance(Wallet wallet, BigDecimal amount);

    /**
     * Fabrique l’enregistrement d’historique associé à l’opération réussie.
     *
     * @param wallet portefeuille après mise à jour
     * @param amount montant de la transaction
     * @param type   type métier
     * @return transaction prête à être sauvegardée
     */
    private Mono<Transaction> createTransaction(Wallet wallet, BigDecimal amount, TransactionType type) {
        return Mono.just(new Transaction(null, wallet.id(), amount, type, TransactionStatus.COMPLETED));
    }

    /**
     * Point d’extension pour notifier un bus d’événements (implémentation actuelle : journal console).
     *
     * @param tx transaction finalisée
     */
    protected void publishEvent(Transaction tx) {
        System.out.println("EVENT PUBLISHED: " + tx);
    }
}
