package com.yowyob.template.infrastructure.adapters.outbound.redis;

import com.yowyob.template.domain.ports.out.WalletBalanceCachePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation Redis (valeurs {@link String} =
 * {@link BigDecimal#toPlainString()}) pour
 * le port {@link WalletBalanceCachePort}. Clés :
 * {@code payment:wallet:balance:{uuid}}.
 */
@Component
@ConditionalOnProperty(prefix = "application.cache.redis", name = "enabled", havingValue = "true")
public class RedisWalletBalanceCacheAdapter implements WalletBalanceCachePort {

    private static final String KEY_PREFIX = "payment:wallet:balance:";

    private final ReactiveStringRedisTemplate redis;

    /**
     * @param redis template réactif chaînes (fourni par Spring Boot lorsque Redis
     *              est configuré)
     */
    public RedisWalletBalanceCacheAdapter(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(UUID walletId) {
        return KEY_PREFIX + walletId;
    }

    @Override
    public Mono<Optional<BigDecimal>> getBalance(UUID walletId) {
        return redis.opsForValue()
                .get(key(walletId))
                .map(s -> Optional.of(new BigDecimal(s)))
                .switchIfEmpty(Mono.just(Optional.empty()));
    }

    @Override
    public Mono<Void> putBalance(UUID walletId, BigDecimal balance, Duration ttl) {
        return redis.opsForValue().set(key(walletId), balance.toPlainString(), ttl).then();
    }

    @Override
    public Mono<Void> evictWallet(UUID walletId) {
        return redis.delete(key(walletId)).then();
    }
}
