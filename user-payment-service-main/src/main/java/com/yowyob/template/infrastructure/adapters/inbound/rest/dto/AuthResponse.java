package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

/**
 * Réponse standard après authentification réussie.
 *
 * @param token JWT Bearer à renvoyer dans l’en-tête {@code Authorization}
 */
public record AuthResponse(String token) {}
