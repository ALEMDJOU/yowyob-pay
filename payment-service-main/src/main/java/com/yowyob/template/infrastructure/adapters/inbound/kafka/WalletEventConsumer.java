package com.yowyob.template.infrastructure.adapters.inbound.kafka;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.infrastructure.adapters.inbound.kafka.event.WalletCreationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Consommateur Kafka : création de portefeuille à partir d’un événement métier {@link WalletCreationEvent}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventConsumer {

    private final WalletUseCase walletUseCase;

    /**
     * Réagit aux messages du topic {@code wallet-create} en créant un portefeuille avec solde zéro.
     *
     * @param event charge utile désérialisée depuis Kafka
     */
    @KafkaListener(topics = "${application.kafka.topics.wallet-create}", groupId = "payment-group")
    public void consumeWalletCreation(WalletCreationEvent event) {
        Wallet domainWallet = new Wallet(null, event.ownerId(), event.ownerName(), BigDecimal.ZERO);

        walletUseCase.createWallet(domainWallet)
                .doOnSuccess(w -> log.info("Wallet créé: {}", w.id()))
                .doOnError(e -> log.error("Erreur création wallet", e))
                .subscribe();
    }
}
