package com.yowyob.template.domain.handler;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;

import reactor.core.publisher.Mono;

/**
 * Débit avec commission fixée à 10% du montant demandé : le solde doit couvrir
 * cette commission.
 */
@Component
public class PaymentHandler extends AbstractTransactionHandler {

    private BigDecimal amountToRemove = new BigDecimal(0);

    /**
     * @param walletRepo dépendance injectée vers la persistance des portefeuilles
     * @param txRepo     dépendance injectée vers la persistance des transactions
     */
    public PaymentHandler(WalletRepositoryPort walletRepo, TransactionRepositoryPort txRepo) {
        super(walletRepo, txRepo);
    }

    /**
     * Vérifie que le montant est strictement positif et que le solde couvre la
     * commission calculée.
     *
     * @param wallet portefeuille à débiter
     * @param amount montant de référence (la commission est dérivée à 10%)
     * @return {@link Mono} du portefeuille inchangé si validation OK
     * @throws IllegalArgumentException propagée via {@link Mono#error(Throwable)}
     *                                  si montant ≤ 0
     * @throws RuntimeException         propagée si solde insuffisant pour la
     *                                  commission
     */
    @Override
    protected Mono<Wallet> validate(Wallet wallet, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Le montant du paiement doit être positif"));
        }

        amountToRemove = amount.multiply(BigDecimal.valueOf(10)).divide(BigDecimal.valueOf(100));
        if (wallet.balance().compareTo(amountToRemove) < 0) {
            return Mono.error(new RuntimeException("Solde insuffisant pour le paiement"));
        }
        return Mono.just(wallet);
    }

    /**
     * Débite la commission calculée lors de {@link #validate(Wallet, BigDecimal)}.
     *
     * @param wallet portefeuille validé
     * @param amount montant d’entrée (inchangé dans le calcul du débit effectif)
     * @return portefeuille avec solde diminué de {@code amountToRemove}
     */
    @Override
    protected Mono<Wallet> applyBalance(Wallet wallet, BigDecimal amount) {
        return Mono.just(wallet.withBalance(wallet.balance().subtract(amountToRemove)));
    }

    /**
     * @return toujours {@link TransactionType#PAYMENT}
     */
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.PAYMENT;
    }
}
