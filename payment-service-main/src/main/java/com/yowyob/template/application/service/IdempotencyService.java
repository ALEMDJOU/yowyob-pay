package com.yowyob.template.application.service;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.yowyob.template.domain.exception.IdempotencyConflictException;
import com.yowyob.template.domain.model.idempotency.IdempotencyContext;
import com.yowyob.template.domain.model.idempotency.IdempotencyNewEntry;
import com.yowyob.template.domain.model.idempotency.IdempotencyOutcome;
import com.yowyob.template.domain.model.idempotency.IdempotencyScope;
import com.yowyob.template.domain.model.idempotency.IdempotencyStored;
import com.yowyob.template.domain.ports.out.IdempotencyPort;
import com.yowyob.template.infrastructure.idempotency.IdempotencyKeyNormalizer;
import com.yowyob.template.infrastructure.idempotency.Sha256Hex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Orchestration idempotence : lecture préalable, exécution métier, stockage de
 * la réponse ou rejouer depuis la base.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final int MAX_KEY_LENGTH = 256;

    private final IdempotencyPort idempotencyPort;
    private final ObjectMapper objectMapper;

    /**
     * Exécute une opération avec idempotence optionnelle.
     *
     * @param scope             périmètre métier
     * @param maybeContext      vide pour court-circuiter l’idempotence (ex. Kafka)
     * @param operation         flux métier à exécuter si aucune ligne existante
     * @param serialize         sérialise la valeur métier en JSON stockable
     * @param deserialize       désérialise le JSON stocké vers la valeur métier
     * @param successHttpStatus statut HTTP de la première réponse réussie (201 ou
     *                          200)
     * @param <T>               type métier
     * @return résultat avec statut HTTP et indicateur de rejouer
     * @throws IdempotencyConflictException si la clé existe déjà avec une autre
     *                                      empreinte de corps
     */
    public <T> Mono<IdempotencyOutcome<T>> execute(
            IdempotencyScope scope,
            Optional<IdempotencyContext> maybeContext,
            Mono<T> operation,
            Function<T, String> serialize,
            Function<String, T> deserialize,
            int successHttpStatus) {

        if (maybeContext.isEmpty()) {
            return operation.map(v -> new IdempotencyOutcome<>(v, successHttpStatus, false));
        }
        IdempotencyContext ctx = maybeContext.get();
        try {
            validateIdempotencyKey(ctx.rawKey());
        } catch (IllegalArgumentException e) {
            return Mono.error(e);
        }
        String keyHash = Sha256Hex.ofUtf8(IdempotencyKeyNormalizer.normalize(ctx.rawKey()));

        return idempotencyPort.findByScopeAndKeyHash(scope, keyHash)
                .flatMap(opt -> opt
                        .map(stored -> replayStored(stored, ctx, deserialize))
                        .orElseGet(() -> executeAndStore(scope, keyHash, ctx, operation, serialize, deserialize,
                                successHttpStatus)));
    }

    private <T> Mono<IdempotencyOutcome<T>> replayStored(
            IdempotencyStored stored,
            IdempotencyContext ctx,
            Function<String, T> deserialize) {
        if (!stored.requestFingerprintSha256Hex().equals(ctx.requestFingerprintSha256Hex())) {
            return Mono.error(new IdempotencyConflictException(
                    "Idempotency-Key déjà utilisée avec un corps de requête différent"));
        }
        try {
            T value = deserialize.apply(stored.responseBodyJson());
            return Mono.just(new IdempotencyOutcome<>(value, stored.httpStatus(), true));
        } catch (Exception ex) {
            return Mono.error(new IllegalStateException("Impossible de relire la réponse idempotente", ex));
        }
    }

    private <T> Mono<IdempotencyOutcome<T>> executeAndStore(
            IdempotencyScope scope,
            String keyHash,
            IdempotencyContext ctx,
            Mono<T> operation,
            Function<T, String> serialize,
            Function<String, T> deserialize,
            int successHttpStatus) {

        return operation.flatMap(result -> {
            String json;
            try {
                json = serialize.apply(result);
            } catch (Exception ex) {
                return Mono.error(ex);
            }
            IdempotencyNewEntry entry = new IdempotencyNewEntry(
                    scope,
                    keyHash,
                    ctx.requestFingerprintSha256Hex(),
                    successHttpStatus,
                    json);
            return idempotencyPort.storeNew(entry)
                    .thenReturn(new IdempotencyOutcome<>(result, successHttpStatus, false))
                    .onErrorResume(DataIntegrityViolationException.class, ex -> handleDuplicateInsert(
                            scope, keyHash, ctx, deserialize, ex));
        });
    }

    private <T> Mono<IdempotencyOutcome<T>> handleDuplicateInsert(
            IdempotencyScope scope,
            String keyHash,
            IdempotencyContext ctx,
            Function<String, T> deserialize,
            DataIntegrityViolationException ex) {

        log.debug("Course sur idempotence détectée, relecture scope={} keyHash={}", scope, keyHash);
        return idempotencyPort.findByScopeAndKeyHash(scope, keyHash)
                .flatMap(opt -> opt
                        .map(stored -> replayStored(stored, ctx, deserialize))
                        .orElse(Mono.error(ex)));
    }

    /**
     * Empreinte SHA-256 hex du JSON canonique d’un corps de requête (même
     * sérialisation que le stockage de réponse côté contrôleurs).
     *
     * @param requestBody objet déjà désérialisé (DTO)
     * @return 64 caractères hex minuscules
     * @throws JsonProcessingException si la sérialisation échoue
     */
    public String fingerprintForRequestBody(Object requestBody) throws JsonProcessingException {
        return Sha256Hex.ofUtf8(objectMapper.writeValueAsString(requestBody));
    }

    /**
     * Empreinte pour une mise à jour de portefeuille : identifiant + corps.
     *
     * @param walletId    identifiant de ressource
     * @param requestBody corps PATCH
     * @return empreinte SHA-256 hex
     * @throws JsonProcessingException si la sérialisation échoue
     */
    public String fingerprintForWalletUpdate(java.util.UUID walletId, Object requestBody)
            throws JsonProcessingException {
        return Sha256Hex.ofUtf8(walletId + "|" + objectMapper.writeValueAsString(requestBody));
    }

    private void validateIdempotencyKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key invalide");
        }
        if (rawKey.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Idempotency-Key trop longue (max " + MAX_KEY_LENGTH + ")");
        }
        for (int i = 0; i < rawKey.length(); i++) {
            char c = rawKey.charAt(i);
            if (c < 32 || c > 126) {
                throw new IllegalArgumentException(
                        "Idempotency-Key : caractères non ASCII imprimables interdits");
            }
        }
    }
}
