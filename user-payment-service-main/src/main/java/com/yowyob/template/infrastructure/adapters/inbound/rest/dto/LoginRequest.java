package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

/**
 * Identifiants de connexion.
 *
 * @param email    email enregistré
 * @param password secret brut
 */
public record LoginRequest(String email, String password) {}
