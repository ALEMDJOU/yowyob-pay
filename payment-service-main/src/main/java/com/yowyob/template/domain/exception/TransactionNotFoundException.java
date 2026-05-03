package com.yowyob.template.domain.exception;

/**
 * Levée lorsqu’aucune transaction n’existe pour l’identifiant consulté.
 */
public class TransactionNotFoundException extends RuntimeException {

    /**
     * @param message détail du problème
     */
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
