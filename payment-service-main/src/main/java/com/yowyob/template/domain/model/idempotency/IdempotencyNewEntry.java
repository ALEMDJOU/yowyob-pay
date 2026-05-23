package com.yowyob.template.domain.model.idempotency;

/**
 * Données nécessaires pour insérer une nouvelle ligne d’idempotence après
 * succès
 * métier.
 *
 * @param scope                       périmètre fonctionnel
 * @param idempotencyKeyHashSha256Hex empreinte SHA-256 (hex) de la clé
 *                                    normalisée
 * @param requestFingerprintSha256Hex empreinte du corps
 * @param httpStatus                  statut HTTP de la réponse stockée
 * @param responseBodyJson            JSON de la réponse
 */
public record IdempotencyNewEntry(
                IdempotencyScope scope,
                String idempotencyKeyHashSha256Hex,
                String requestFingerprintSha256Hex,
                int httpStatus,
                String responseBodyJson) {
}
