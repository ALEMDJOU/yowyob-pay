package com.yowyob.template.infrastructure.config;

import com.yowyob.template.domain.ports.out.WalletBalanceCachePort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import com.yowyob.template.infrastructure.adapters.outbound.cache.CachingWalletRepositoryDecorator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Enregistre le décorateur de cache portefeuille lorsque
 * {@code application.cache.redis.enabled=true}.
 * Le bean {@code postgresWalletRepository} reste l’adapter R2DBC ; le bean
 * {@code @Primary}
 * exposé aux services est le décorateur.
 */
@Configuration
public class WalletCacheConfiguration {

    /**
     * @param postgres           adapter R2DBC (qualifier explicite)
     * @param walletBalanceCache port Redis
     * @param walletBalanceTtl   TTL ISO-8601 (ex. {@code PT30S})
     * @return portefeuille décoré
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "application.cache.redis", name = "enabled", havingValue = "true")
    public WalletRepositoryPort cachingWalletRepository(
            @Qualifier("postgresWalletRepository") WalletRepositoryPort postgres,
            WalletBalanceCachePort walletBalanceCache,
            @Value("${application.cache.wallet-balance-ttl}") Duration walletBalanceTtl) {
        return new CachingWalletRepositoryDecorator(postgres, walletBalanceCache, walletBalanceTtl);
    }
}
