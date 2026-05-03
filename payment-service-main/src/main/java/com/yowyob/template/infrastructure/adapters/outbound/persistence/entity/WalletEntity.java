package com.yowyob.template.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Projection relationnelle d’un {@link com.yowyob.template.domain.model.Wallet}.
 * Le champ {@link #isNew} pilote insert vs update pour Spring Data JDBC/R2DBC.
 */
@Table("wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private BigDecimal balance;

    @Transient
    private boolean isNew = false;

    /**
     * @return identifiant de persistance
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * @return {@code true} pour forcer un INSERT, {@code false} pour un UPDATE
     */
    @Override
    public boolean isNew() {
        return isNew || id == null;
    }

    /**
     * Fabrique une entité marquée pour insertion.
     *
     * @param id         clé choisie
     * @param ownerId    propriétaire
     * @param ownerName  libellé
     * @param balance    solde initial
     * @return entité prête pour un INSERT
     */
    public static WalletEntity createNew(UUID id, UUID ownerId, String ownerName, BigDecimal balance) {
        return new WalletEntity(id, ownerId, ownerName, balance, true);
    }
}
