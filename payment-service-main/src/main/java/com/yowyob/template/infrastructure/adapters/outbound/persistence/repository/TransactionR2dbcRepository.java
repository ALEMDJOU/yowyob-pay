package com.yowyob.template.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Accès Spring Data R2DBC pour la table {@code transactions}.
 */
public interface TransactionR2dbcRepository extends R2dbcRepository<TransactionEntity, UUID> {

    /**
     * @param walletId portefeuille cible
     * @return flux du plus récent au plus ancien (index
     *         {@code wallet_id, created_at DESC})
     */
    Flux<TransactionEntity> findAllByWalletIdOrderByCreatedAtDesc(UUID walletId);
}
