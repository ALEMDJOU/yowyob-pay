package com.yowyob.template.application.service;

import com.yowyob.template.domain.exception.TransactionNotFoundException;
import com.yowyob.template.domain.exception.WalletNotFoundException;
import com.yowyob.template.domain.handler.AbstractTransactionHandler;
import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.in.TransactionUseCase;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routage des transactions vers le bon {@link AbstractTransactionHandler} et consultations associées.
 */
@Service
public class TransactionService implements TransactionUseCase {

    private final Map<TransactionType, AbstractTransactionHandler> handlersMap;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final WalletRepositoryPort walletRepositoryPort;

    /**
     * Construit la carte type → handler à partir des beans Spring disponibles.
     *
     * @param handlers                liste injectée de tous les handlers concrets
     * @param transactionRepositoryPort accès persistance transactions
     * @param walletRepositoryPort      accès persistance portefeuilles
     */
    public TransactionService(List<AbstractTransactionHandler> handlers, TransactionRepositoryPort transactionRepositoryPort, WalletRepositoryPort walletRepositoryPort) {
        this.handlersMap = handlers.stream()
                .collect(Collectors.toMap(AbstractTransactionHandler::getTransactionType, Function.identity()));
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.walletRepositoryPort = walletRepositoryPort;
    }

    /**
     * Délègue au handler du {@link Transaction#type()} pour exécuter le flux métier complet.
     *
     * @param transaction doit contenir un type supporté et un {@code walletId} valide
     * @return transaction persistée après traitement
     * @throws IllegalArgumentException si le type n’a pas de handler
     */
    @Override
    public Mono<Transaction> createTransaction(Transaction transaction) {
        AbstractTransactionHandler handler = handlersMap.get(transaction.type());
        if (handler == null) {
            return Mono.error(new IllegalArgumentException("Type de transaction inconnu : " + transaction.type()));
        }
        return handler.process(transaction.walletId(), transaction.amount());
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
     * Vérifie d’abord l’existence du portefeuille, puis retourne l’historique des transactions liées.
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
