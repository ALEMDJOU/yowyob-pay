package com.yowyob.template.infrastructure.adapters.outbound.persistence;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionStatus;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.TransactionEntity;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.repository.TransactionR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Persistance R2DBC des transactions et mapping enum ↔ colonnes texte.
 */
@Component
@RequiredArgsConstructor
public class PostgresTransactionAdapter implements TransactionRepositoryPort {

    private final TransactionR2dbcRepository repository;

    /**
     * Upsert d’une transaction : conversion domaine → entité puis sauvegarde.
     *
     * @param transaction modèle à persister
     * @return transaction avec identifiants issues de la base
     */
    @Override
    public Mono<Transaction> save(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity(
                transaction.id(),
                transaction.walletId(),
                transaction.amount(),
                transaction.type().name(),
                transaction.status().name()
        );

        return repository.save(entity)
                .map(this::mapToDomain);
    }

    /**
     * @param id clé primaire
     * @return transaction ou {@link Mono#empty()}
     */
    @Override
    public Mono<Transaction> getTransactionById(UUID id) {
        return repository.findById(id)
                .map(this::mapToDomain);
    }

    /**
     * @param walletId filtre par portefeuille
     * @return historique complet pour ce portefeuille
     */
    @Override
    public Flux<Transaction> getTransactionsByWalletId(UUID walletId) {
        return repository.findAllByWalletId(walletId)
                .map(this::mapToDomain);
    }

    /**
     * Convertit l’entité persistante vers le modèle riche (énumérations).
     *
     * @param entity ligne SQL
     * @return transaction du domaine
     * @throws IllegalArgumentException si les chaînes type/statut ne correspondent pas aux enums
     */
    private Transaction mapToDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getWalletId(),
                entity.getAmount(),
                TransactionType.valueOf(entity.getType()),
                TransactionStatus.valueOf(entity.getStatus())
        );
    }
}
