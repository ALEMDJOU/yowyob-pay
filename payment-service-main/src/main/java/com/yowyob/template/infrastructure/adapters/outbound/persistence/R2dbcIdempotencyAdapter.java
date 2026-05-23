package com.yowyob.template.infrastructure.adapters.outbound.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.yowyob.template.domain.model.idempotency.IdempotencyNewEntry;
import com.yowyob.template.domain.model.idempotency.IdempotencyScope;
import com.yowyob.template.domain.model.idempotency.IdempotencyStored;
import com.yowyob.template.domain.ports.out.IdempotencyPort;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.IdempotencyRequestEntity;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.repository.IdempotencyRequestR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Adaptateur R2DBC du port {@link IdempotencyPort}.
 */
@Component
@RequiredArgsConstructor
public class R2dbcIdempotencyAdapter implements IdempotencyPort {

    private final IdempotencyRequestR2dbcRepository repository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Optional<IdempotencyStored>> findByScopeAndKeyHash(IdempotencyScope scope, String keyHashSha256Hex) {
        return repository.findByScopeAndIdempotencyKeyHash(scope.name(), keyHashSha256Hex)
                .map(e -> Optional.of(new IdempotencyStored(
                        e.getRequestFingerprint(),
                        e.getHttpStatus(),
                        e.getResponseBody())))
                .defaultIfEmpty(Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> storeNew(IdempotencyNewEntry entry) {
        IdempotencyRequestEntity entity = new IdempotencyRequestEntity(
                UUID.randomUUID(),
                entry.scope().name(),
                entry.idempotencyKeyHashSha256Hex(),
                entry.requestFingerprintSha256Hex(),
                entry.httpStatus(),
                entry.responseBodyJson(),
                Instant.now());
        return repository.save(entity).then();
    }
}
