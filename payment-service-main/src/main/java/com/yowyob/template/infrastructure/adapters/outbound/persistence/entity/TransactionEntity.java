package com.yowyob.template.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

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
}
