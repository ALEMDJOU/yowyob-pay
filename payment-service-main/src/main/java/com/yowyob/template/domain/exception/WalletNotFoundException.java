package com.yowyob.template.domain.exception;

/**
 * Levée lorsqu’aucun portefeuille ne correspond à l’identifiant demandé.
 */
public class WalletNotFoundException extends RuntimeException {

    /**
     * @param message détail du problème (identifiant ou contexte)
     */
    public WalletNotFoundException(String message) {
        super(message);
    }
}
