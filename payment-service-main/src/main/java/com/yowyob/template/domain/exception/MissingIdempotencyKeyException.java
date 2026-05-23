package com.yowyob.template.domain.exception;

/**
 * Levée lorsque l’en-tête obligatoire {@code Idempotency-Key} est absent ou
 * vide
 * sur une route protégée par idempotence.
 */
public class MissingIdempotencyKeyException extends RuntimeException {

    /**
     * @param message détail lisible côté API
     */
    public MissingIdempotencyKeyException(String message) {
        super(message);
    }
}
