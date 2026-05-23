package com.yowyob.template.infrastructure.adapters.outbound.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Intégration Redis (Testcontainers) pour
 * {@link RedisWalletBalanceCacheAdapter}.
 */
@Testcontainers(disabledWithoutDocker = true)
class RedisWalletBalanceCacheAdapterIT {

        @Container
        @SuppressWarnings("resource")
        private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                        .withExposedPorts(6379);

        @Test
        void putGetEvictRoundTrip() {
                RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(
                                REDIS.getHost(), REDIS.getMappedPort(6379));
                LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
                LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, clientConfig);
                factory.afterPropertiesSet();

                ReactiveStringRedisTemplate template = new ReactiveStringRedisTemplate(factory);
                RedisWalletBalanceCacheAdapter adapter = new RedisWalletBalanceCacheAdapter(template);
                UUID id = UUID.randomUUID();
                BigDecimal balance = new BigDecimal("123.4500");

                StepVerifier.create(adapter.getBalance(id))
                                .assertNext(opt -> assertTrue(opt.isEmpty()))
                                .verifyComplete();

                StepVerifier.create(adapter.putBalance(id, balance, Duration.ofSeconds(60))
                                .thenMany(adapter.getBalance(id)))
                                .assertNext(opt -> assertEquals(0, opt.orElseThrow().compareTo(balance)))
                                .verifyComplete();

                StepVerifier.create(adapter.evictWallet(id).thenMany(adapter.getBalance(id)))
                                .assertNext(opt -> assertTrue(opt.isEmpty()))
                                .verifyComplete();

                factory.destroy();
        }
}
