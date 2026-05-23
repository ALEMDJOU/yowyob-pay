package com.yowyob.template.infrastructure.adapters.outbound.persistence;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.WalletEntity;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.repository.WalletR2dbcRepository;

import com.yowyob.template.infrastructure.mappers.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 
 * Adapter R2DBC PostgreSQL pour le port {@link com.yowyob.template.domain.ports.out.WalletRepositoryPort}.
 */
@Component("postgresWalletRepository")
@RequiredArgsConstructor
public class PostgresWalletAdapter implements WalletRepositoryPort {

    private final WalletR2dbcRepository repository;
    private final WalletMapper mapper;

    /**
     * @param id clé du portefeuille
     * @return le portefeuille ou {@link Mono#empty()} si absent
     */
    @Override
    public Mono<Wallet> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * 
     * Insertion : marque l’entité comme nouvelle avant {@link WalletR2dbcRepository#save}.
     *
     * @param wallet données domaine
     * @return portefeuille après insertion
     */
    @Override
    public Mono<Wallet> save(Wallet wallet) {
        WalletEntity entity = mapper.toEntity(wallet);
        entity.setNew(true);

        return repository.save(entity)
                .map(mapper::toDomain);
    }

    /**
     * @param ownerId propriétaire recherché
     * @return au plus un portefeuille
     */
    @Override
    public Mono<Wallet> findByOwnerId(UUID ownerId) {
        return repository.findByOwnerId(ownerId)
                .map(mapper::toDomain);
    }

    /**
     * @param page index 0-based
     * @param size taille de page
     * @return page matérialisée avec totaux
     */
    @Override
    public Mono<WalletPage> findWalletsPage(int page, int size) {
        long offset = (long) page * size;
        return repository.count()
                .flatMap(total -> repository.findAllWalletsPaged(size, offset)
                        .map(mapper::toDomain)
                        .collectList()
                        .map(list -> new WalletPage(
                                list,
                                page,
                                size,
                                total,
                                total == 0L ? 0 : (int) Math.ceil((double) total / (double) size))));
    }

    /**
     * @param id identifiant à supprimer
     * @return complétion vide
     */
    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    /**
     * Met à jour propriétaire et solde si la ligne existe.
     *
     * @param wallet jeton domaine avec identifiant existant
     * @return portefeuille mis à jour
     *                          
     * @throws RuntimeException propagée via {@link Mono#error(Throwable)} si aucune ligne pour mise à jour
     */
    @Override
    public Mono<Wallet> updateWallet(Wallet wallet) {
        return repository.findById(wallet.id())

                .switchIfEmpty(Mono.error(new RuntimeException("Wallet not found for update")))

                .map(entity -> {
                    entity.setOwnerId(wallet.ownerId());
                    entity.setBalance(wallet.balance());
                    entity.setNew(false);
                    return entity;
                })

                .flatMap(repository::save)

                .map(mapper::toDomain);
    }
}
