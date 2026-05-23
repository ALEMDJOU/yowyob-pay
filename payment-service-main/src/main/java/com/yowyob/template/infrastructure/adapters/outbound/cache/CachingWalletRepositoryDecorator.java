package com.yowyob.template.infrastructure.adapters.outbound.cache;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.ports.out.WalletBalanceCachePort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * Décorateur du {@link WalletRepositoryPort} : lecture cache du solde sur
 * {@link #findById},
 * remplissage sur lecture propriétaire, pas de cache sur la pagination ;
 * invalidation
 * stricte après {@link #updateWallet} / {@link #deleteById}.
 */
@RequiredArgsConstructor
public class CachingWalletRepositoryDecorator implements WalletRepositoryPort {

    private final WalletRepositoryPort delegate;
    private final WalletBalanceCachePort cache;
    private final Duration walletBalanceTtl;

    @Override
    public Mono<Wallet> findById(UUID id) {
        return cache.getBalance(id)
                .flatMap(opt -> delegate.findById(id)
                        .flatMap(wallet -> {
                            if (opt.isPresent()) {
                                return Mono.just(wallet.withBalance(opt.get()));
                            }
                            return cache.putBalance(wallet.id(), wallet.balance(), walletBalanceTtl)
                                    .thenReturn(wallet);
                        }));
    }

    @Override
    public Mono<Wallet> save(Wallet wallet) {
        return delegate.save(wallet)
                .flatMap(w -> cache.putBalance(w.id(), w.balance(), walletBalanceTtl).thenReturn(w));
    }

    @Override
    public Mono<Wallet> findByOwnerId(UUID ownerId) {
        return delegate.findByOwnerId(ownerId)
                .flatMap(w -> cache.putBalance(w.id(), w.balance(), walletBalanceTtl).thenReturn(w));
    }

    @Override
    public Mono<WalletPage> findWalletsPage(int page, int size) {
        return delegate.findWalletsPage(page, size);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return cache.evictWallet(id).then(delegate.deleteById(id));
    }

    @Override
    public Mono<Wallet> updateWallet(Wallet wallet) {
        return delegate.updateWallet(wallet)
                .flatMap(w -> cache.evictWallet(w.id()).thenReturn(w));
    }
}
