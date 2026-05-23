package com.yowyob.template.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.template.domain.exception.TransactionNotFoundException;
import com.yowyob.template.domain.exception.WalletNotFoundException;
import com.yowyob.template.domain.handler.AbstractTransactionHandler;
import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.idempotency.IdempotencyContext;
import com.yowyob.template.domain.model.idempotency.IdempotencyOutcome;
import com.yowyob.template.domain.model.idempotency.IdempotencyScope;
import com.yowyob.template.domain.ports.in.TransactionUseCase;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.TransactionResponse;
import com.yowyob.template.infrastructure.config.BusinessProperties;
import com.yowyob.template.infrastructure.mappers.TransactionMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Routage des transactions vers le bon {@link AbstractTransactionHandler} et
 * consultations associées.
 */
@Service
public class TransactionService implements TransactionUseCase {

    private final Map<TransactionType, AbstractTransactionHandler> handlersMap;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final WalletRepositoryPort walletRepositoryPort;
    private final BusinessProperties businessProperties;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    private final TransactionMapper transactionMapper;

    /**
     * Construit la carte type → handler à partir des beans Spring disponibles.
     *
     * @param handlers                  liste injectée de tous les handlers concrets
     * @param transactionRepositoryPort accès persistance transactions
     * @param walletRepositoryPort      accès persistance portefeuilles
     * @param businessProperties        plafonds métier (montant max par
     *                                  transaction)
     * @param idempotencyService        orchestration idempotence HTTP
     * @param objectMapper              sérialisation JSON des réponses stockées
     * @param transactionMapper         conversions transaction
     */
    public TransactionService(List<AbstractTransactionHandler> handlers,
            TransactionRepositoryPort transactionRepositoryPort, WalletRepositoryPort walletRepositoryPort,
            BusinessProperties businessProperties,
            IdempotencyService idempotencyService,
            ObjectMapper objectMapper,
            TransactionMapper transactionMapper) {
        this.handlersMap = handlers.stream()
                .collect(Collectors.toMap(AbstractTransactionHandler::getTransactionType, Function.identity()));
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.walletRepositoryPort = walletRepositoryPort;
        this.businessProperties = businessProperties;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
        this.transactionMapper = transactionMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<IdempotencyOutcome<Transaction>> createTransactionWithIdempotency(
            Transaction transaction,
            Optional<IdempotencyContext> idempotencyContext) {
        IdempotencyScope scope = transaction.type() == TransactionType.RECHARGE
                ? IdempotencyScope.TRANSACTION_CREATE_RECHARGE
                : IdempotencyScope.TRANSACTION_CREATE_PAYMENT;
        return idempotencyService.execute(
                scope,
                idempotencyContext,
                createTransactionCore(transaction),
                this::serializeTransaction,
                this::deserializeTransaction,
                201);
    }

    private Mono<Transaction> createTransactionCore(Transaction transaction) {
        BigDecimal max = businessProperties.maxTransactionAmount();
        if (transaction.amount() != null && transaction.amount().compareTo(max) > 0) {
            return Mono.error(new IllegalArgumentException(
                    "Montant supérieur au plafond autorisé (" + max + ")"));
        }
        AbstractTransactionHandler handler = handlersMap.get(transaction.type());
        if (handler == null) {
            return Mono.error(new IllegalArgumentException("Type de transaction inconnu : " + transaction.type()));
        }
        return handler.process(transaction.walletId(), transaction.amount());
    }

    private String serializeTransaction(Transaction transaction) {
        try {
            return objectMapper.writeValueAsString(transactionMapper.toResponse(transaction));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Sérialisation transaction impossible", e);
        }
    }

    private Transaction deserializeTransaction(String json) {
        try {
            TransactionResponse response = objectMapper.readValue(json, TransactionResponse.class);
            return transactionMapper.toDomain(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Désérialisation transaction impossible", e);
        }
    }

    /**
     * @param id identifiant de transaction
     * @return l’enregistrement trouvé
     * @throws TransactionNotFoundException si absent
     */
    @Override
    public Mono<Transaction> getTransactionById(UUID id) {
        return transactionRepositoryPort.getTransactionById(id)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException("Transaction not found")));
    }

    /**
     * Vérifie d’abord l’existence du portefeuille, puis retourne l’historique des
     * transactions liées.
     *
     * @param walletId identifiant du portefeuille
     * @return flux des transactions pour ce portefeuille
     * @throws WalletNotFoundException si le portefeuille n’existe pas
     */
    @Override
    public Flux<Transaction> getTransactionsByWalletId(UUID walletId) {
        return walletRepositoryPort.findById(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Not found")))
                .flatMapMany(wallet -> transactionRepositoryPort.getTransactionsByWalletId(walletId));
    }
}
