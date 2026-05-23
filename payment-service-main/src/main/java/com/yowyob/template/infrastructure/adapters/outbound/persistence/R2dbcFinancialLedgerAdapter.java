package com.yowyob.template.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.FinancialLedgerPort;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;

import reactor.core.publisher.Mono;

/**
 * Nom : {@code R2dbcFinancialLedgerAdapter}
 * <p>
 * Description : implémente {@link FinancialLedgerPort} en enchaînant
 * {@link WalletRepositoryPort#updateWallet(Wallet)} puis
 * {@link TransactionRepositoryPort#save(Transaction)} dans un bloc
 * {@link TransactionalOperator#transactional(reactor.core.publisher.Mono)}.
 * </p>
 */
@Component
public class R2dbcFinancialLedgerAdapter implements FinancialLedgerPort {

    private final TransactionalOperator transactionalOperator;
    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    /**
     * Nom : constructeur
     * <p>
     * Description : injecte l’opérateur transactionnel et les ports de persistance
     * sous-jacents.
     * </p>
     *
     * @param transactionalOperator     opérateur Spring pour bornes
     *                                  transactionnelles
     * @param walletRepositoryPort      mise à jour du solde portefeuille
     * @param transactionRepositoryPort persistance de la ligne transaction
     */
    public R2dbcFinancialLedgerAdapter(
            TransactionalOperator transactionalOperator,
            WalletRepositoryPort walletRepositoryPort,
            TransactionRepositoryPort transactionRepositoryPort) {
        this.transactionalOperator = transactionalOperator;
        this.walletRepositoryPort = walletRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Transaction> commitWalletMutationAndTransaction(
            Wallet updatedWallet, Transaction transactionToPersist) {
        return transactionalOperator.transactional(
                walletRepositoryPort
                        .updateWallet(updatedWallet)
                        .then(transactionRepositoryPort.save(transactionToPersist)));
    }
}
