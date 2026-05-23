package com.yowyob.template.infrastructure.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitaire : empreinte SHA-256 d’une chaîne UTF-8 encodée en hexadécimal
 * minuscule (64 caractères).
 */
public final class Sha256Hex {

    private Sha256Hex() {
    }

    /**
     * @param utf8 texte en clair
     * @return 64 caractères hex minuscules
     * @throws IllegalStateException si l’algorithme SHA-256 n’est pas disponible
     */
    public static String ofUtf8(String utf8) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(utf8.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
