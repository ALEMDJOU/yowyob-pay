package com.yowyob.template.infrastructure.idempotency;

import java.util.Locale;

/**
 * Normalisation de la clé d’idempotence HTTP (insensible à la casse, espaces
 * de tête / fin retirés).
 */
public final class IdempotencyKeyNormalizer {

    private IdempotencyKeyNormalizer() {
    }

    /**
     * @param raw valeur brute de l’en-tête
     * @return clé normalisée pour hachage / comparaison logique
     */
    public static String normalize(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT);
    }
}
