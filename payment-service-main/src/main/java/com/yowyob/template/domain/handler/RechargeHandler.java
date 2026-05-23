package com.yowyob.template.domain.handler;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.FinancialLedgerPort;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;

import reactor.core.publisher.Mono;

/**
 * Crédit du portefeuille : ajoute le montant au solde existant (logique pure,
 * sans état mutable).
 */
@Component
public class RechargeHandler extends AbstractTransactionHandler {

    /**
     * Nom : constructeur
     * <p>
     * Description : injecte les ports de persistance pour la chaîne
     * {@link #process}.
     * </p>
     *
     * @param walletRepository      accès aux portefeuilles
     * @param transactionRepository accès aux transactions
     * @param financialLedgerPort   persistance atomique wallet + ligne transaction
     */
    public RechargeHandler(
            WalletRepositoryPort walletRepository,
            TransactionRepositoryPort transactionRepository,
            FinancialLedgerPort financialLedgerPort) {
        super(walletRepository, transactionRepository, financialLedgerPort);
    }

    /**
     * Nom : {@code computeUpdatedWallet}
     * <p>
     * Description : refuse les montants non strictement positifs ; sinon retourne
     * le
     * portefeuille avec solde augmenté du montant de recharge.
     * </p>
     *
     * @param wallet portefeuille cible
     * @param amount montant à créditer
     * @return portefeuille avec nouveau solde (non encore persisté)
     * @throws IllegalArgumentException via {@link Mono#error(Throwable)} si montant
     *                                  ≤ 0
     */
    @Override
    protected Mono<Wallet> computeUpdatedWallet(Wallet wallet, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Le montant de la recharge doit être positif"));
        }
        BigDecimal newBalance = wallet.balance().add(amount);
        return Mono.just(wallet.withBalance(newBalance));
    }

    /**
     * @return toujours {@link TransactionType#RECHARGE}
     */
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.RECHARGE;
    }
}
