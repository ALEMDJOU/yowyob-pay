package com.yowyob.template.domain.exception;

/**
 * Levée lorsqu’un client réutilise une {@code Idempotency-Key} déjà consommée
 * avec un corps de requête différent (empreinte distincte).
 */
public class IdempotencyConflictException extends RuntimeException {

    /**
     * @param message détail exposé au client (RFC 7807)
     */
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
