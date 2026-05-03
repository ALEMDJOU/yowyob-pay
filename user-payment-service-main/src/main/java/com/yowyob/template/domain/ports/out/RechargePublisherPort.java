package com.yowyob.template.domain.ports.out;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Publication asynchrone d’une demande de recharge vers le bus (Kafka).
 */
public interface RechargePublisherPort {

    /**
     * Émet un événement pour que le payment-service crédite le portefeuille indiqué.
     *
     * @param targetWalletId identifiant du wallet dans le payment-service
     * @param amount         montant à créditer
     * @return complétion lorsque le message est accepté par le client producteur
     */
    Mono<Void> publishRechargeEvent(UUID targetWalletId, BigDecimal amount);
}
