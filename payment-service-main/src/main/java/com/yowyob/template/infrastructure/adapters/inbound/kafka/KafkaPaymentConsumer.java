package com.yowyob.template.infrastructure.adapters.inbound.kafka;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionStatus;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.ports.in.TransactionUseCase;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.infrastructure.adapters.inbound.kafka.event.PaymentCommissionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Consommateur des événements de commission : déclenche un {@link com.yowyob.template.domain.model.TransactionType#PAYMENT}
 * proportionnel au taux configuré.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentConsumer {

    private final TransactionUseCase transactionUseCase;
    private final WalletUseCase walletUseCase;


    @Value("${application.payment.commission-rate:0.1}")
    private BigDecimal commissionRate;

    /**
     * Calcule le montant à prélever ({@code baseAmount × commission-rate}) et exécute une transaction PAYMENT.
     *
     * @param event propriétaire et montant de base annoncé par le producteur
     */
    @KafkaListener(topics = "${application.kafka.topics.payment-commission}", groupId = "payment-group")
    public void consumePaymentCommission(PaymentCommissionEvent event) {


        // LOGIQUE MÉTIER : Calcul du montant à retirer (Pourcentage * MontantBase)
        BigDecimal amountToDeduct = event.baseAmount().multiply(commissionRate);

        walletUseCase.getWalletByOwnerId(event.ownerId())
                .flatMap(wallet -> {
                    Transaction domainTx = new Transaction(
                            null,
                            wallet.id(),
                            amountToDeduct,
                            TransactionType.PAYMENT,
                            TransactionStatus.PENDING
                    );

                    return transactionUseCase.createTransaction(domainTx);
                })
                .doOnSuccess(tx -> log.info("Commission prélevée avec succès. Tx ID: {}", tx.id()))
                .doOnError(e -> log.error("Échec du prélèvement de commission pour owner {}", event.ownerId(), e))
                .subscribe();
    }
}
