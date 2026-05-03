package com.yowyob.template.application.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yowyob.template.domain.exception.WalletNotFoundException;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implémentation des cas d’usage portefeuille : création, lecture, mise à jour
 * et suppression.
 */
@Service
@RequiredArgsConstructor
public class WalletService implements WalletUseCase {

    /** Accès persistant aux portefeuilles. */
    public final WalletRepositoryPort walletRepositoryPort;

    /**
     * Crée un portefeuille avec identifiant généré si absent et solde initial par
     * défaut 1000 si absent.
     *
     * @param wallet données saisies ; {@code id} et {@code balance} peuvent être
     *               complétés automatiquement
     * @return le portefeuille persisté
     */
    @Override
    public Mono<Wallet> createWallet(Wallet wallet) {
        Wallet toSave = new Wallet(
                wallet.id() != null ? wallet.id() : UUID.randomUUID(),
                wallet.ownerId(),
                wallet.ownerName(),
                wallet.balance() != null ? wallet.balance() : BigDecimal.valueOf(1000));

        return walletRepositoryPort.save(toSave);
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
     * Met à jour propriétaire et nom si fournis, conserve le solde existant.
     *
     * @param wallet doit contenir un {@code id} valide
     * @return portefeuille après mise à jour
     * @throws WalletNotFoundException si l’identifiant est inconnu
     */
    @Override
    public Mono<Wallet> updateWallet(Wallet wallet) {
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
     * @return flux de tous les portefeuilles en base
     */
    @Override
    public Flux<Wallet> getAllWallets() {
        return walletRepositoryPort.findAllWallets();
    }
}
