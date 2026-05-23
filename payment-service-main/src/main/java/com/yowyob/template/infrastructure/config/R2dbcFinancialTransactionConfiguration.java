package com.yowyob.template.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import io.r2dbc.spi.ConnectionFactory;

/**
 * Nom : {@code R2dbcFinancialTransactionConfiguration}
 * <p>
 * Description : enregistre le gestionnaire transactionnel R2DBC réactif et
 * l’opérateur {@link TransactionalOperator} utilisés pour grouper mises à jour
 * portefeuille et écritures transaction comptable.
 * </p>
 */
@Configuration
public class R2dbcFinancialTransactionConfiguration {

    /**
     * Nom : {@code r2dbcTransactionManager}
     * <p>
     * Description : adapte {@link ConnectionFactory} au modèle transactionnel
     * Spring réactif.
     * </p>
     *
     * @param connectionFactory fabrique de connexions R2DBC (bean auto-configuré)
     * @return gestionnaire transactionnel réactif
     */
    @Bean
    public ReactiveTransactionManager r2dbcTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Nom : {@code transactionalOperator}
     * <p>
     * Description : opérateur réutilisable pour encadrer des {@code Mono} /
     * {@code Flux}
     * dans une transaction unique.
     * </p>
     *
     * @param transactionManager gestionnaire {@link R2dbcTransactionManager}
     * @return opérateur transactionnel réactif
     */
    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }
}
