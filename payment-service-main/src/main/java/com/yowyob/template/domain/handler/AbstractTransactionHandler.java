package com.yowyob.template.domain.handler;

import java.math.BigDecimal;
import java.util.UUID;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionStatus;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.FinancialLedgerPort;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;

import reactor.core.publisher.Mono;

/**
 * Orchestration réactive type « template method » pour une transaction : calcul
 * du nouveau solde (sans état mutable partagé entre requêtes), persistance
 * atomique
 * du solde et de l’historique via {@link FinancialLedgerPort}.
 */
public abstract class AbstractTransactionHandler {

    protected final WalletRepositoryPort walletRepository;
    protected final TransactionRepositoryPort transactionRepository;
    protected final FinancialLedgerPort financialLedgerPort;

    /**
     * Nom : constructeur
     * <p>
     * Description : injecte les ports de persistance et le port d’écriture atomique
     * portefeuille + transaction.
     * </p>
     *
     * @param walletRepository      accès lecture / hors transaction atomique si
     *                              besoin
     * @param transactionRepository accès lecture (historique) ; l’écriture critique
     *                              passe par {@link FinancialLedgerPort}
     * @param financialLedgerPort   persistance atomique update wallet + insert
     *                              transaction
     */
    protected AbstractTransactionHandler(
            WalletRepositoryPort walletRepository,
            TransactionRepositoryPort transactionRepository,
            FinancialLedgerPort financialLedgerPort) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.financialLedgerPort = financialLedgerPort;
    }

    /**
     * Indique le type de transaction géré par cette implémentation.
     *
     * @return {@link TransactionType} associé au handler
     */
    public abstract TransactionType getTransactionType();

    /**
     * Nom : {@code process}
     * <p>
     * Description : charge le portefeuille, applique la logique métier via
     * {@link #computeUpdatedWallet(Wallet, BigDecimal)}, puis
     * {@link FinancialLedgerPort#commitWalletMutationAndTransaction(Wallet, Transaction)}
     * pour valider solde et ligne d’historique dans une seule transaction SQL.
     * </p>
     *
     * @param walletId identifiant du portefeuille cible
     * @param amount   montant métier (interprété selon le handler concret)
     * @return la transaction persistée à l’état {@link TransactionStatus#COMPLETED}
     * @throws RuntimeException via
     *                          {@link reactor.core.publisher.Mono#error(Throwable)}
     *                          si portefeuille absent ou règle métier violée
     */
    public Mono<Transaction> process(UUID walletId, BigDecimal amount) {
        TransactionType type = getTransactionType();
        return walletRepository.findById(walletId)
                .switchIfEmpty(Mono.error(new RuntimeException("Wallet not found")))
                .flatMap(wallet -> computeUpdatedWallet(wallet, amount))
                .flatMap(updatedWallet -> createTransaction(updatedWallet, amount, type)
                        .flatMap(tx -> financialLedgerPort.commitWalletMutationAndTransaction(updatedWallet, tx)))
                .doOnSuccess(this::publishEvent);
    }

    /**
     * Nom : {@code computeUpdatedWallet}
     * <p>
     * Description : valide les règles métier et retourne le portefeuille avec le
     * solde
     * déjà recalculé (sans mutation d’état d’instance entre deux appels réactifs).
     * </p>
     *
     * @param wallet portefeuille courant issu de la base
     * @param amount montant de référence de l’opération
     * @return portefeuille prêt à être persisté via {@link FinancialLedgerPort}
     */
    protected abstract Mono<Wallet> computeUpdatedWallet(Wallet wallet, BigDecimal amount);

    /**
     * Fabrique l’enregistrement d’historique associé à l’opération réussie.
     *
     * @param wallet portefeuille après mise à jour logique (solde recalculé)
     * @param amount montant de la transaction
     * @param type   type métier
     * @return transaction prête à être sauvegardée dans le cadre atomique
     */
    private Mono<Transaction> createTransaction(Wallet wallet, BigDecimal amount, TransactionType type) {
        return Mono.just(new Transaction(null, wallet.id(), amount, type, TransactionStatus.COMPLETED));
    }

    /**
     * Point d’extension pour notifier un bus d’événements (implémentation actuelle
     * : journal console).
     *
     * @param tx transaction finalisée
     */
    protected void publishEvent(Transaction tx) {
        System.out.println("EVENT PUBLISHED: " + tx);
    }
}
