package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enveloppe REST paginée pour la liste des portefeuilles.
 *
 * @param content       éléments de la page courante
 * @param page          index de page (0-based)
 * @param size          taille de page demandée
 * @param totalElements nombre total d’éléments
 * @param totalPages    nombre total de pages
 */
@Schema(description = "Réponse paginée de portefeuilles")
public record PagedWalletsResponse(
                @Schema(description = "Contenu de la page") List<WalletResponse> content,
                @Schema(description = "Index de page (0-based)", example = "0") int page,
                @Schema(description = "Taille de page", example = "20") int size,
                @Schema(description = "Nombre total d’éléments") long totalElements,
                @Schema(description = "Nombre total de pages") int totalPages) {
}
