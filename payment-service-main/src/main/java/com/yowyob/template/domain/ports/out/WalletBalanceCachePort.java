package com.yowyob.template.domain.ports.out;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant : cache réactif du solde par portefeuille (lecture seule côté
 * cache ;
 * invalidation / remplissage après écriture).
 */
public interface WalletBalanceCachePort {

    /**
     * Lit le solde mis en cache pour un portefeuille.
     *
     * @param walletId identifiant du portefeuille
     * @return solde en cache ou {@link Optional#empty()} si absent (miss)
     */
    Mono<Optional<BigDecimal>> getBalance(UUID walletId);

    /**
     * Enregistre le solde avec TTL (remplissage après lecture base ou après
     * création).
     *
     * @param walletId identifiant
     * @param balance  solde à figer
     * @param ttl      durée de vie
     * @return complétion vide
     */
    Mono<Void> putBalance(UUID walletId, BigDecimal balance, Duration ttl);

    /**
     * Supprime toute entrée de cache pour ce portefeuille (invalidation stricte
     * après mutation).
     *
     * @param walletId identifiant
     * @return complétion vide
     */
    Mono<Void> evictWallet(UUID walletId);
}
