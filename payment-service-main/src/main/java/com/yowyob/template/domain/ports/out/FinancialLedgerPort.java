package com.yowyob.template.domain.ports.out;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.Wallet;

import reactor.core.publisher.Mono;

/**
 * Port sortant : persistance atomique (une unité transactionnelle R2DBC) du
 * solde
 * portefeuille et de la ligne d’historique comptable associée.
 */
public interface FinancialLedgerPort {

    /**
     * Nom : {@code commitWalletMutationAndTransaction}
     * <p>
     * Description : applique la mutation de solde ({@code UPDATE wallets}) puis
     * insère la ligne {@code transactions} dans la même transaction SQL ; en cas
     * d’échec sur l’une des deux opérations, la transaction est annulée (rollback).
     * </p>
     * <p>
     * L’idempotence HTTP (en-tête {@code Idempotency-Key}) est gérée au-dessus de
     * ce
     * port ; ce port ne dédoublonne pas les appels.
     * </p>
     *
     * @param updatedWallet        portefeuille avec solde déjà recalculé côté
     *                             domaine
     * @param transactionToPersist transaction à insérer (statut, type, montant déjà
     *                             fixés)
     * @return la transaction persistée avec identifiants issus de la base
     * @throws org.springframework.dao.DataAccessException via
     *                                                     {@link Mono#error(Throwable)}
     *                                                     en cas d’erreur SQL
     */
    Mono<Transaction> commitWalletMutationAndTransaction(Wallet updatedWallet, Transaction transactionToPersist);
}
