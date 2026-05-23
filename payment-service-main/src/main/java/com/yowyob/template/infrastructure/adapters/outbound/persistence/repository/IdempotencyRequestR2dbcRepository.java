package com.yowyob.template.infrastructure.adapters.outbound.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.IdempotencyRequestEntity;

import reactor.core.publisher.Mono;

/**
 * Accès R2DBC aux lignes d’idempotence.
 */
public interface IdempotencyRequestR2dbcRepository extends ReactiveCrudRepository<IdempotencyRequestEntity, UUID> {

    /**
     * @param scope              valeur
     *                           {@link com.yowyob.template.domain.model.idempotency.IdempotencyScope#name()}
     * @param idempotencyKeyHash empreinte SHA-256 hex de la clé normalisée
     * @return ligne existante ou vide
     */
    Mono<IdempotencyRequestEntity> findByScopeAndIdempotencyKeyHash(String scope, String idempotencyKeyHash);
}
