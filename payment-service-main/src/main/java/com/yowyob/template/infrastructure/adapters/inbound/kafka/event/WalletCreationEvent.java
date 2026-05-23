package com.yowyob.template.infrastructure.adapters.inbound.kafka.event;

import java.util.UUID;

/**
 * Schéma Kafka **v1** : message minimal annonçant la création logique d’un
 * portefeuille pour un propriétaire.
 *
 * @param ownerId   référence propriétaire
 * @param ownerName nom pour initialiser le portefeuille
 */
public record WalletCreationEvent(UUID ownerId, String ownerName) {
}
