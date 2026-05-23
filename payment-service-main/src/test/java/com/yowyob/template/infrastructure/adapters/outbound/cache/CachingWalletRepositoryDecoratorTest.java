package com.yowyob.template.infrastructure.adapters.outbound.cache;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.ports.out.WalletBalanceCachePort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachingWalletRepositoryDecoratorTest {

    private static final Duration TTL = Duration.ofSeconds(30);

    @Mock
    private WalletRepositoryPort delegate;

    @Mock
    private WalletBalanceCachePort cache;

    private CachingWalletRepositoryDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator = new CachingWalletRepositoryDecorator(delegate, cache, TTL);
    }

    @Test
    void findById_cacheHit_mergesBalance() {
        UUID id = UUID.randomUUID();
        Wallet fromDb = new Wallet(id, UUID.randomUUID(), "x", new BigDecimal("1"));
        when(cache.getBalance(id)).thenReturn(Mono.just(Optional.of(new BigDecimal("999"))));
        when(delegate.findById(id)).thenReturn(Mono.just(fromDb));

        StepVerifier.create(decorator.findById(id))
                .assertNext(w -> assertEquals(0, w.balance().compareTo(new BigDecimal("999"))))
                .verifyComplete();

        verify(cache, never()).putBalance(any(), any(), any(Duration.class));
    }

    @Test
    void updateWallet_evictsCache() {
        UUID id = UUID.randomUUID();
        Wallet w = new Wallet(id, UUID.randomUUID(), "x", new BigDecimal("10"));
        when(delegate.updateWallet(w)).thenReturn(Mono.just(w));
        when(cache.evictWallet(id)).thenReturn(Mono.empty());

        StepVerifier.create(decorator.updateWallet(w)).expectNextCount(1).verifyComplete();

        verify(cache).evictWallet(eq(id));
    }

    @Test
    void findWalletsPage_delegatesWithoutCache() {
        WalletPage page = new WalletPage(List.of(), 0, 20, 0L, 0);
        when(delegate.findWalletsPage(0, 20)).thenReturn(Mono.just(page));

        StepVerifier.create(decorator.findWalletsPage(0, 20)).expectNext(page).verifyComplete();

        verify(cache, never()).getBalance(any());
    }
}
