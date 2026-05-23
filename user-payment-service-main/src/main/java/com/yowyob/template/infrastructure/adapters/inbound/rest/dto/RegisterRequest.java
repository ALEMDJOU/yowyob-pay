package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données d’inscription d’un agent.
 *
 * @param name     nom affiché
 * @param email    adresse unique
 * @param password mot de passe en clair (haché par le service)
 */
public record RegisterRequest(
                @NotBlank(message = "name est obligatoire") @Size(max = 120, message = "name trop long") String name,
                @NotBlank(message = "email est obligatoire") @Email(message = "email invalide") @Size(max = 255) String email,
                @NotBlank(message = "password est obligatoire") @Size(min = 8, max = 128, message = "password : entre 8 et 128 caractères") String password) {
}
