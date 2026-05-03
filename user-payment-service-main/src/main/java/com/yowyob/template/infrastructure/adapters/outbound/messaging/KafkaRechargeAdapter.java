package com.yowyob.template.infrastructure.adapters.outbound.messaging;

import com.yowyob.template.domain.ports.out.RechargePublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Publie sur Kafka une demande de recharge au format attendu par le payment-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaRechargeAdapter implements RechargePublisherPort {

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topics.transaction-recharge}")
    private String rechargeTopic;

    /**
     * Charge utile JSON pour la création de recharge distante.
     *
     * @param walletId identifiant du portefeuille cible
     * @param amount   montant à créditer
     */
    record TransactionEvent(UUID walletId, BigDecimal amount) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> publishRechargeEvent(UUID targetWalletId, BigDecimal amount) {
        TransactionEvent event = new TransactionEvent(targetWalletId, amount);

        log.info("Envoi demande recharge Kafka -> Wallet: {} Montant: {}", targetWalletId, amount);

        return kafkaTemplate.send(rechargeTopic, targetWalletId.toString(), event)
                .then();
    }
}
