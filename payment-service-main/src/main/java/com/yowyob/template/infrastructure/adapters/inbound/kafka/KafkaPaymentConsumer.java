package com.yowyob.template.infrastructure.adapters.inbound.kafka;

import java.math.BigDecimal;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.yowyob.template.domain.financial.CommissionCalculator;
import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionStatus;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.ports.in.TransactionUseCase;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.infrastructure.adapters.inbound.kafka.event.PaymentCommissionEvent;
import com.yowyob.template.infrastructure.config.PaymentProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consommateur des événements de commission : déclenche un
 * {@link com.yowyob.template.domain.model.TransactionType#PAYMENT}
 * proportionnel au
 * taux unique {@link PaymentProperties#commissionRate()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentConsumer {

    private final TransactionUseCase transactionUseCase;
    private final WalletUseCase walletUseCase;
    private final PaymentProperties paymentProperties;

    /**
     * Nom : {@code consumePaymentCommission}
     * <p>
     * Description : calcule la commission avec
     * {@link CommissionCalculator#commissionFromBaseAmount(java.math.BigDecimal, java.math.BigDecimal)}
     * puis enchaîne
     * {@link TransactionUseCase#createTransaction(com.yowyob.template.domain.model.Transaction)}.
     * </p>
     *
     * @param event propriétaire et montant de base annoncé par le producteur
     */
    @KafkaListener(topics = "${application.kafka.topics.payment-commission}", groupId = "payment-group")
    public void consumePaymentCommission(PaymentCommissionEvent event) {
        BigDecimal rate = paymentProperties.commissionRate();
        BigDecimal amountToDeduct = CommissionCalculator.commissionFromBaseAmount(event.baseAmount(), rate);

        walletUseCase.getWalletByOwnerId(event.ownerId())
                .flatMap(wallet -> {
                    Transaction domainTx = new Transaction(
                            null,
                            wallet.id(),
                            amountToDeduct,
                            TransactionType.PAYMENT,
                            TransactionStatus.PENDING);

                    return transactionUseCase.createTransaction(domainTx);
                })
                .doOnSuccess(tx -> log.info("Commission prélevée avec succès. Tx ID: {}", tx.id()))
                .doOnError(e -> log.error("Échec du prélèvement de commission pour owner {}", event.ownerId(), e))
                .subscribe();
    }
}
