package com.yowyob.template.domain.model;

import java.util.List;

/**
 * Page de portefeuilles (métadonnées + contenu matérialisé pour l’API REST).
 *
 * @param content       éléments de la page courante
 * @param page          index de page (0-based)
 * @param size          taille demandée
 * @param totalElements nombre total d’éléments en base
 * @param totalPages    nombre total de pages (0 si aucun élément)
 */
public record WalletPage(
                List<Wallet> content,
                int page,
                int size,
                long totalElements,
                int totalPages) {
}
