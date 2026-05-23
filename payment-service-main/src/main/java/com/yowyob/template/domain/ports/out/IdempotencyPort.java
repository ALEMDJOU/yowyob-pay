package com.yowyob.template.domain.ports.out;

import java.util.Optional;

import com.yowyob.template.domain.model.idempotency.IdempotencyNewEntry;
import com.yowyob.template.domain.model.idempotency.IdempotencyScope;
import com.yowyob.template.domain.model.idempotency.IdempotencyStored;

import reactor.core.publisher.Mono;

/**
 * Persistance des réponses idempotentes (détection de rejouer et détection de
 * conflit corps / clé).
 */
public interface IdempotencyPort {

    /**
     * Recherche une ligne existante pour un couple (périmètre, empreinte de clé).
     *
     * @param scope            périmètre métier de la route
     * @param keyHashSha256Hex SHA-256 hex de la clé normalisée
     * @return ligne trouvée ou vide
     */
    Mono<Optional<IdempotencyStored>> findByScopeAndKeyHash(IdempotencyScope scope, String keyHashSha256Hex);

    /**
     * Insère une nouvelle ligne après succès métier (unicité {@code scope} +
     * {@code keyHash}).
     *
     * @param entry données à persister
     * @return complétion vide
     * @throws org.springframework.dao.DataIntegrityViolationException en cas de
     *                                                                 course
     *                                                                 sur la même
     *                                                                 clé (à
     *                                                                 traiter en
     *                                                                 couche
     *                                                                 applicative
     *                                                                 par un second
     *                                                                 {@link #findByScopeAndKeyHash})
     */
    Mono<Void> storeNew(IdempotencyNewEntry entry);
}
