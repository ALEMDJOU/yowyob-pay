package com.yowyob.template.domain.model.idempotency;

/**
 * Contexte d’idempotence HTTP : clé client et empreinte du corps (déjà
 * normalisée / hachée côté présentation).
 *
 * @param rawKey                      valeur brute de l’en-tête
 *                                    {@code Idempotency-Key}
 * @param requestFingerprintSha256Hex empreinte SHA-256 (hex) du corps canonique
 */
public record IdempotencyContext(String rawKey, String requestFingerprintSha256Hex) {
}
