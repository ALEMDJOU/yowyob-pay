package com.yowyob.template.application.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.template.domain.exception.WalletNotFoundException;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.model.idempotency.IdempotencyContext;
import com.yowyob.template.domain.model.idempotency.IdempotencyOutcome;
import com.yowyob.template.domain.model.idempotency.IdempotencyScope;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.WalletResponse;
import com.yowyob.template.infrastructure.mappers.WalletMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Implémentation des cas d’usage portefeuille : création, lecture, mise à jour
 * et suppression.
 */
@Service
@RequiredArgsConstructor
public class WalletService implements WalletUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    private final WalletMapper walletMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<IdempotencyOutcome<Wallet>> createWalletWithIdempotency(
            Wallet wallet,
            Optional<IdempotencyContext> idempotencyContext) {
        Wallet toSave = new Wallet(
                wallet.id() != null ? wallet.id() : UUID.randomUUID(),
                wallet.ownerId(),
                wallet.ownerName(),
                wallet.balance() != null ? wallet.balance() : BigDecimal.valueOf(1000));

        return idempotencyService.execute(
                IdempotencyScope.WALLET_CREATE,
                idempotencyContext,
                walletRepositoryPort.save(toSave),
                this::serializeWallet,
                this::deserializeWallet,
                201);
    }

    /**
     * @param ownerId identifiant du propriétaire
     * @return le portefeuille associé
     * @throws WalletNotFoundException si aucune ligne ne correspond
     */
    @Override
    public Mono<Wallet> getWalletByOwnerId(UUID ownerId) {
        return walletRepositoryPort.findByOwnerId(ownerId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Optional<Wallet>> findWalletByOwnerIdOptional(UUID ownerId) {
        return walletRepositoryPort.findByOwnerId(ownerId).map(Optional::of).defaultIfEmpty(Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<IdempotencyOutcome<Wallet>> updateWalletWithIdempotency(
            Wallet wallet,
            Optional<IdempotencyContext> idempotencyContext) {
        return idempotencyService.execute(
                IdempotencyScope.WALLET_UPDATE,
                idempotencyContext,
                updateWalletCore(wallet),
                this::serializeWallet,
                this::deserializeWallet,
                200);
    }

    private Mono<Wallet> updateWalletCore(Wallet wallet) {
        return walletRepositoryPort.findById(wallet.id())
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")))
                .flatMap(existingWallet -> {
                    Wallet walletToUpdate = new Wallet(
                            existingWallet.id(),
                            wallet.ownerId() == null ? existingWallet.ownerId() : wallet.ownerId(),
                            wallet.ownerName() == null ? existingWallet.ownerName() : wallet.ownerName(),
                            existingWallet.balance());

                    return walletRepositoryPort.updateWallet(walletToUpdate);
                });
    }

    private String serializeWallet(Wallet wallet) {
        try {
            return objectMapper.writeValueAsString(walletMapper.toResponse(wallet));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Sérialisation portefeuille impossible", e);
        }
    }

    private Wallet deserializeWallet(String json) {
        try {
            WalletResponse response = objectMapper.readValue(json, WalletResponse.class);
            return walletMapper.toDomain(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Désérialisation portefeuille impossible", e);
        }
    }

    /**
     * @param id identifiant du portefeuille à supprimer
     * @return complétion vide une fois la suppression effectuée
     * @throws WalletNotFoundException si inconnu
     */
    @Override
    public Mono<Void> deleteWallet(UUID id) {
        return walletRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")))
                .flatMap(wallet -> walletRepositoryPort.deleteById(wallet.id()));
    }

    /**
     * @param id identifiant technique du portefeuille
     * @return le portefeuille trouvé
     * @throws WalletNotFoundException sinon
     */
    @Override
    public Mono<Wallet> getWalletById(UUID id) {
        return walletRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Not found")));
    }

    /**
     * @param page index 0-based
     * @param size taille de page (validée côté contrôleur)
     * @return page domaine
     */
    @Override
    public Mono<WalletPage> getWalletsPage(int page, int size) {
        return walletRepositoryPort.findWalletsPage(page, size);
    }
}
