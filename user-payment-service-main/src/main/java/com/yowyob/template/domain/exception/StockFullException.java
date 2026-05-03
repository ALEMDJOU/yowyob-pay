package com.yowyob.template.domain.exception;

/**
 * Erreur fonctionnelle lorsque la capacité externe (stock, quota) est saturée.
 */
public class StockFullException extends RuntimeException {

    /**
     * @param message description lisible côté API
     */
    public StockFullException(String message) {
        super(message);
    }
}
