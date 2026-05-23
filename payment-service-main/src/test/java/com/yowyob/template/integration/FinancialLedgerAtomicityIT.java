package com.yowyob.template.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionStatus;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.FinancialLedgerPort;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;

import reactor.test.StepVerifier;

/**
 * Vérifie l’atomicité {@link FinancialLedgerPort} : commit conjoint wallet +
 * transaction, rollback si la seconde écriture échoue (violation FK).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class FinancialLedgerAtomicityIT {

        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

        @DynamicPropertySource
        static void registerDatasource(DynamicPropertyRegistry registry) {
                registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://%s:%d/%s".formatted(
                                postgres.getHost(),
                                postgres.getMappedPort(5432),
                                postgres.getDatabaseName()));
                registry.add("spring.r2dbc.username", postgres::getUsername);
                registry.add("spring.r2dbc.password", postgres::getPassword);
                registry.add("spring.liquibase.url", postgres::getJdbcUrl);
                registry.add("spring.liquibase.user", postgres::getUsername);
                registry.add("spring.liquibase.password", postgres::getPassword);
                registry.add("spring.liquibase.enabled", () -> "true");
                registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.xml");
        }

        @Autowired
        private FinancialLedgerPort financialLedgerPort;

        @Autowired
        private WalletRepositoryPort walletRepositoryPort;

        @Autowired
        private TransactionRepositoryPort transactionRepositoryPort;

        @Test
        void commitPersistsUpdatedWalletAndTransaction() {
                Wallet initial = new Wallet(null, UUID.randomUUID(), "owner-it", new BigDecimal("100.0000"));
                AtomicReference<UUID> walletId = new AtomicReference<>();

                StepVerifier.create(walletRepositoryPort.save(initial)
                                .doOnNext(w -> walletId.set(w.id()))
                                .flatMap(saved -> {
                                        Wallet updated = saved.withBalance(new BigDecimal("50.0000"));
                                        Transaction tx = new Transaction(
                                                        null,
                                                        saved.id(),
                                                        new BigDecimal("10.0000"),
                                                        TransactionType.RECHARGE,
                                                        TransactionStatus.COMPLETED);
                                        return financialLedgerPort.commitWalletMutationAndTransaction(updated, tx);
                                })
                                .timeout(Duration.ofSeconds(30)))
                                .assertNext(tx -> assertThat(tx.walletId()).isEqualTo(walletId.get()))
                                .verifyComplete();

                StepVerifier.create(walletRepositoryPort.findById(walletId.get()))
                                .assertNext(w -> assertThat(w.balance()).isEqualByComparingTo("50.0000"))
                                .verifyComplete();

                StepVerifier.create(transactionRepositoryPort.getTransactionsByWalletId(walletId.get()).collectList())
                                .assertNext(list -> assertThat(list).hasSize(1))
                                .verifyComplete();
        }

        @Test
        void rollbackWhenTransactionViolatesForeignKey() {
                Wallet initial = new Wallet(null, UUID.randomUUID(), "owner-rollback", new BigDecimal("100.0000"));
                AtomicReference<UUID> walletId = new AtomicReference<>();

                StepVerifier.create(walletRepositoryPort.save(initial).doOnNext(w -> walletId.set(w.id())))
                                .assertNext(w -> assertThat(w.balance()).isEqualByComparingTo("100.0000"))
                                .verifyComplete();

                Wallet updated = new Wallet(walletId.get(), initial.ownerId(), initial.ownerName(),
                                new BigDecimal("50.0000"));
                Transaction badFk = new Transaction(
                                null,
                                UUID.randomUUID(),
                                new BigDecimal("1.0000"),
                                TransactionType.RECHARGE,
                                TransactionStatus.COMPLETED);

                StepVerifier.create(
                                financialLedgerPort.commitWalletMutationAndTransaction(updated, badFk)
                                                .timeout(Duration.ofSeconds(30)))
                                .verifyError();

                StepVerifier.create(walletRepositoryPort.findById(walletId.get()))
                                .assertNext(w -> assertThat(w.balance()).isEqualByComparingTo("100.0000"))
                                .verifyComplete();

                StepVerifier.create(transactionRepositoryPort.getTransactionsByWalletId(walletId.get()).collectList())
                                .assertNext(list -> assertThat(list).isEmpty())
                                .verifyComplete();
        }
}
