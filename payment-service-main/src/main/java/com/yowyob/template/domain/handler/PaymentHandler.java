package com.yowyob.template.domain.handler;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.yowyob.template.domain.financial.CommissionCalculator;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.FinancialLedgerPort;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import com.yowyob.template.infrastructure.config.PaymentProperties;

import reactor.core.publisher.Mono;

/**
 * Débit d’une commission calculée à partir du montant de référence et du taux
 * {@code application.payment.commission-rate} : le solde doit couvrir cette
 * commission.
 * Logique sans état mutable d’instance partagé entre requêtes concurrentes.
 */
@Component
public class PaymentHandler extends AbstractTransactionHandler {

    private final PaymentProperties paymentProperties;

    /**
     * Nom : constructeur
     * <p>
     * Description : injecte les ports de persistance et la configuration du taux de
     * commission.
     * </p>
     *
     * @param walletRepo        accès aux portefeuilles
     * @param txRepo            accès aux transactions
     * @param paymentProperties taux de commission (0–1)
     */
    public PaymentHandler(
            WalletRepositoryPort walletRepo,
            TransactionRepositoryPort txRepo,
            PaymentProperties paymentProperties,
            FinancialLedgerPort financialLedgerPort) {
        super(walletRepo, txRepo, financialLedgerPort);
        this.paymentProperties = paymentProperties;
    }

    /**
     * Nom : {@code computeUpdatedWallet}
     * <p>
     * Description : vérifie un montant strictement positif, calcule la commission
     * avec
     * arrondi HALF_UP à 4 décimales, contrôle le solde et retourne le portefeuille
     * avec
     * solde diminué de la commission.
     * </p>
     *
     * @param wallet portefeuille à débiter
     * @param amount montant de référence pour le calcul de la commission
     * @return portefeuille avec nouveau solde (non encore persisté)
     * @throws IllegalArgumentException via {@link Mono#error(Throwable)} si montant
     *                                  ≤ 0
     * @throws RuntimeException         via {@link Mono#error(Throwable)} si solde
     *                                  insuffisant
     */
    @Override
    protected Mono<Wallet> computeUpdatedWallet(Wallet wallet, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Le montant du paiement doit être positif"));
        }
        BigDecimal rate = paymentProperties.commissionRate();
        BigDecimal commission = CommissionCalculator.commissionFromBaseAmount(amount, rate);
        if (wallet.balance().compareTo(commission) < 0) {
            return Mono.error(new RuntimeException("Solde insuffisant pour le paiement"));
        }
        BigDecimal newBalance = wallet.balance().subtract(commission);
        return Mono.just(wallet.withBalance(newBalance));
    }

    /**
     * @return toujours {@link TransactionType#PAYMENT}
     */
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.PAYMENT;
    }
}
