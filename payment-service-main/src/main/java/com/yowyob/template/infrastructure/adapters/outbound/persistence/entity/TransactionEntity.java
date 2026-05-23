package com.yowyob.template.infrastructure.adapters.outbound.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ligne {@code transactions} : types et statuts stockés en texte (noms d’énum).
 */
@Table("transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {
    @Id
    private UUID id;
    private UUID walletId;
    private BigDecimal amount;
    private String type;
    private String status;
    /** Horodatage de création (colonne {@code created_at}, défaut côté base). */
    private Instant createdAt;
}
