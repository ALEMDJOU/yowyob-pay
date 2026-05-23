package com.yowyob.template.domain.model.idempotency;

/**
 * Résultat d’une opération potentiellement idempotente : valeur métier, statut
 * HTTP à renvoyer au client et indicateur de rejouer depuis le stockage.
 *
 * @param value      entité métier (ou DTO équivalent côté service)
 * @param httpStatus code HTTP à appliquer (201 création, 200 mise à jour, ou
 *                   rejoué)
 * @param fromReplay {@code true} si la réponse provient du stockage idempotence
 * @param <T>        type de la charge utile métier
 */
public record IdempotencyOutcome<T>(T value, int httpStatus, boolean fromReplay) {
}
