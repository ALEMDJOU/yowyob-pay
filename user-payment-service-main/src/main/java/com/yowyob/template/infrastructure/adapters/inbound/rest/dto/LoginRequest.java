package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Identifiants de connexion.
 *
 * @param email    email enregistré
 * @param password secret brut
 */
public record LoginRequest(
                @NotBlank(message = "email est obligatoire") @Email(message = "email invalide") @Size(max = 255) String email,
                @NotBlank(message = "password est obligatoire") @Size(max = 128) String password) {
}
