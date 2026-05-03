package com.yowyob.template.domain.exception;

/**
 * Levée lorsque la capacité externe (stock, quota) est dépassée ou indisponible.
 */
public class StockFullException extends RuntimeException {

    /**
     * @param message cause fonctionnelle ou technique
     */
    public StockFullException(String message) {
        super(message);
    }
}