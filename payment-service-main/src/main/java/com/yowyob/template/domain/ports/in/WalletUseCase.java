package com.yowyob.template.domain.ports.in;

import java.util.Optional;
import java.util.UUID;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.model.idempotency.IdempotencyContext;
import com.yowyob.template.domain.model.idempotency.IdempotencyOutcome;

import reactor.core.publisher.Mono;

/**
 * Cas d’usage pour la gestion du cycle de vie des portefeuilles.
 */
public interface WalletUseCase {

    /**
     * Crée un nouveau portefeuille.
     *
     * @param wallet données du portefeuille (souvent sans id, assigné côté
     *               stockage)
     * @return le portefeuille persisté
     */
    default Mono<Wallet> createWallet(Wallet wallet) {
        return createWalletWithIdempotency(wallet, Optional.empty()).map(IdempotencyOutcome::value);
    }

    /**
     * Crée un portefeuille avec prise en charge optionnelle de l’idempotence HTTP.
     *
     * @param wallet             données du portefeuille
     * @param idempotencyContext vide pour flux internes (Kafka, jobs)
     * @return résultat avec statut HTTP et indicateur de rejouer
     */
    Mono<IdempotencyOutcome<Wallet>> createWalletWithIdempotency(
            Wallet wallet,
            Optional<IdempotencyContext> idempotencyContext);

    /**
     * Récupère le portefeuille d’un propriétaire (un seul attendu par
     * propriétaire).
     *
     * @param ownerId identifiant du propriétaire
     * @return le portefeuille ou une erreur {@code NotFound} si absent
     */
    Mono<Wallet> getWalletByOwnerId(UUID ownerId);

    /**
     * Met à jour un portefeuille existant.
     *
     * @param wallet entité complète à enregistrer
     * @return le portefeuille mis à jour
     */
    default Mono<Wallet> updateWallet(Wallet wallet) {
        return updateWalletWithIdempotency(wallet, Optional.empty()).map(IdempotencyOutcome::value);
    }

    /**
     * Met à jour un portefeuille avec idempotence HTTP optionnelle.
     *
     * @param wallet             portefeuille cible (identifiant obligatoire)
     * @param idempotencyContext contexte {@code Idempotency-Key} + empreinte du
     *                           corps
     * @return résultat avec statut HTTP (200) et indicateur de rejouer
     */
    Mono<IdempotencyOutcome<Wallet>> updateWalletWithIdempotency(
            Wallet wallet,
            Optional<IdempotencyContext> idempotencyContext);

    /**
     * Recherche optionnelle d’un portefeuille par propriétaire (absence = vide,
     * sans exception).
     *
     * @param ownerId identifiant propriétaire
     * @return portefeuille encapsulé ou vide
     */
    Mono<Optional<Wallet>> findWalletByOwnerIdOptional(UUID ownerId);

    /**
     * Supprime définitivement un portefeuille par identifiant.
     *
     * @param id identifiant du portefeuille
     * @return complétion réactive quand la suppression est faite
     */
    Mono<Void> deleteWallet(UUID id);

    /**
     * Récupère un portefeuille par identifiant.
     *
     * @param id identifiant unique
     * @return le portefeuille ou une erreur {@code NotFound} si absent
     */
    Mono<Wallet> getWalletById(UUID id);

    /**
     * Liste paginée des portefeuilles (paramètres validés côté contrôleur).
     *
     * @param page index 0-based
     * @param size nombre d’éléments par page (1 à 50)
     * @return enveloppe de page domaine
     */
    Mono<WalletPage> getWalletsPage(int page, int size);
}
