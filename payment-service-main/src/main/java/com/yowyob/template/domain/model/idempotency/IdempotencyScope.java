package com.yowyob.template.domain.model.idempotency;

/**
 * Périmètre fonctionnel d’une requête idempotente (stocké en base sur la ligne
 * {@code idempotency_requests.scope}).
 */
public enum IdempotencyScope {

    /**
     * Création de portefeuille via {@code POST /api/v1/wallets}.
     */
    WALLET_CREATE,

    /**
     * Mise à jour de portefeuille via {@code PATCH /api/v1/wallets/{id}}.
     */
    WALLET_UPDATE,

    /**
     * Création d’une recharge via {@code POST /api/v1/transactions}.
     */
    TRANSACTION_CREATE_RECHARGE,

    /**
     * Création d’un paiement via {@code POST /api/v1/transactions/payment}.
     */
    TRANSACTION_CREATE_PAYMENT
}
