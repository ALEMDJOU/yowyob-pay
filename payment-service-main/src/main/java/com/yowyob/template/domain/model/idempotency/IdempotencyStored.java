package com.yowyob.template.domain.model.idempotency;

/**
 * Ligne d’idempotence déjà persistée (rejouable si l’empreinte du corps
 * correspond).
 *
 * @param requestFingerprintSha256Hex empreinte du corps de la première requête
 *                                    réussie
 * @param httpStatus                  code HTTP renvoyé lors de la première
 *                                    réponse
 * @param responseBodyJson            corps JSON sérialisé de la première
 *                                    réponse
 */
public record IdempotencyStored(
                String requestFingerprintSha256Hex,
                int httpStatus,
                String responseBodyJson) {
}
