package com.yowyob.template.domain.handler;

import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Crédit du portefeuille : ajoute le montant au solde existant.
 */
@Component
public class RechargeHandler extends AbstractTransactionHandler {

    /**
     * @param walletRepository     persistance des portefeuilles
     * @param transactionRepository persistance des transactions
     */
    public RechargeHandler(WalletRepositoryPort walletRepository, TransactionRepositoryPort transactionRepository) {
        super(walletRepository, transactionRepository);
    }

    /**
     * Valide que le montant de recharge est strictement positif.
     *
     * @param wallet  portefeuille cible
     * @param amount  montant à créditer
     * @return le portefeuille inchangé si la validation réussit
     * @throws IllegalArgumentException via {@link Mono#error(Throwable)} si montant ≤ 0
     */
    @Override
    protected Mono<Wallet> validate(Wallet wallet, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Le montant de la recharge doit être positif"));
        }
        return Mono.just(wallet);
    }

    /**
     * Ajoute le montant au solde courant.
     *
     * @param wallet  portefeuille à créditer
     * @param amount  montant positif
     * @return portefeuille avec solde augmenté
     */
    @Override
    protected Mono<Wallet> applyBalance(Wallet wallet, BigDecimal amount) {
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
