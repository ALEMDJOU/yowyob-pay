package com.yowyob.template.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.WalletEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Accès Spring Data R2DBC pour la table {@code wallets}.
 */
public interface WalletR2dbcRepository extends R2dbcRepository<WalletEntity, UUID> {

    /**
     * @param ownerId identifiant métier du propriétaire
     * @return la ligne associée ou vide si aucune correspondance
     */
    Mono<WalletEntity> findByOwnerId(UUID ownerId);

    /**
     * Fenêtre paginée (tri stable par {@code id}).
     *
     * @param limit  nombre maximum de lignes
     * @param offset décalage SQL
     * @return lignes de la page
     */
    @Query("SELECT * FROM wallets ORDER BY id ASC LIMIT :limit OFFSET :offset")
    Flux<WalletEntity> findAllWalletsPaged(@Param("limit") int limit, @Param("offset") long offset);
}
