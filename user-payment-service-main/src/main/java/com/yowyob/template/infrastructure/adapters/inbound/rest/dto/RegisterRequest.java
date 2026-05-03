package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

/**
 * Données d’inscription d’un agent.
 *
 * @param name     nom affiché
 * @param email    adresse unique
 * @param password mot de passe en clair (haché par le service)
 */
public record RegisterRequest(String name, String email, String password) {}
