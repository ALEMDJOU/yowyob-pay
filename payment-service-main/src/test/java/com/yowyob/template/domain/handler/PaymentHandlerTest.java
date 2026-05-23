package com.yowyob.template.domain.handler;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.out.FinancialLedgerPort;
import com.yowyob.template.domain.ports.out.TransactionRepositoryPort;
import com.yowyob.template.domain.ports.out.WalletRepositoryPort;
import com.yowyob.template.infrastructure.config.PaymentProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentHandlerTest {

        @Mock
        private WalletRepositoryPort walletRepository;

        @Mock
        private TransactionRepositoryPort transactionRepository;

        @Mock
        private FinancialLedgerPort financialLedgerPort;

        private PaymentHandler handler;

        @BeforeEach
        void setUp() {
                handler = new PaymentHandler(
                                walletRepository,
                                transactionRepository,
                                new PaymentProperties(new BigDecimal("0.1")),
                                financialLedgerPort);
                lenient()
                                .when(financialLedgerPort.commitWalletMutationAndTransaction(any(Wallet.class),
                                                any(Transaction.class)))
                                .thenAnswer(invocation -> {
                                        Transaction t = invocation.getArgument(1, Transaction.class);
                                        return Mono.just(new Transaction(UUID.randomUUID(), t.walletId(), t.amount(),
                                                        t.type(), t.status()));
                                });
        }

        @Test
        void commissionAtTenPercentDebitsBalance() {
                UUID walletId = UUID.randomUUID();
                Wallet wallet = new Wallet(walletId, UUID.randomUUID(), "owner", new BigDecimal("100"));
                when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));

                StepVerifier.create(handler.process(walletId, new BigDecimal("50")))
                                .assertNext(tx -> {
                                        assertEquals(TransactionType.PAYMENT, tx.type());
                                        assertEquals(0, tx.amount().compareTo(new BigDecimal("50")));
                                })
                                .verifyComplete();

                verify(financialLedgerPort).commitWalletMutationAndTransaction(
                                argThat(w -> w.balance().compareTo(new BigDecimal("95")) == 0),
                                any(Transaction.class));
        }

        @Test
        void commissionUsesConfiguredRate() {
                handler = new PaymentHandler(
                                walletRepository,
                                transactionRepository,
                                new PaymentProperties(new BigDecimal("0.05")),
                                financialLedgerPort);

                UUID walletId = UUID.randomUUID();
                Wallet wallet = new Wallet(walletId, UUID.randomUUID(), "owner", new BigDecimal("100"));
                when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));

                StepVerifier.create(handler.process(walletId, new BigDecimal("100")))
                                .assertNext(tx -> assertEquals(TransactionType.PAYMENT, tx.type()))
                                .verifyComplete();

                verify(financialLedgerPort).commitWalletMutationAndTransaction(
                                argThat(w -> w.balance().compareTo(new BigDecimal("95")) == 0),
                                any(Transaction.class));
        }

        @Test
        void insufficientBalanceFails() {
                UUID walletId = UUID.randomUUID();
                Wallet wallet = new Wallet(walletId, UUID.randomUUID(), "owner", new BigDecimal("3"));
                when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));

                StepVerifier.create(handler.process(walletId, new BigDecimal("100")))
                                .verifyErrorMatches(e -> e.getMessage() != null
                                                && e.getMessage().contains("Solde insuffisant"));

                verify(financialLedgerPort, never())
                                .commitWalletMutationAndTransaction(any(Wallet.class), any(Transaction.class));
        }

        @Test
        void negativeAmountRejected() {
                UUID walletId = UUID.randomUUID();
                Wallet wallet = new Wallet(walletId, UUID.randomUUID(), "owner", new BigDecimal("100"));
                when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));

                StepVerifier.create(handler.process(walletId, new BigDecimal("-1")))
                                .verifyError(IllegalArgumentException.class);

                verify(financialLedgerPort, never())
                                .commitWalletMutationAndTransaction(any(Wallet.class), any(Transaction.class));
        }

        @Test
        void concurrentProcessesDoNotMixBalances() {
                UUID id1 = UUID.randomUUID();
                UUID id2 = UUID.randomUUID();
                Wallet w1 = new Wallet(id1, UUID.randomUUID(), "a", new BigDecimal("100"));
                Wallet w2 = new Wallet(id2, UUID.randomUUID(), "b", new BigDecimal("200"));
                when(walletRepository.findById(id1)).thenReturn(Mono.just(w1));
                when(walletRepository.findById(id2)).thenReturn(Mono.just(w2));

                StepVerifier.create(
                                Mono.zip(
                                                handler.process(id1, new BigDecimal("50")),
                                                handler.process(id2, new BigDecimal("100"))))
                                .assertNext(tuple -> {
                                        assertEquals(id1, tuple.getT1().walletId());
                                        assertEquals(id2, tuple.getT2().walletId());
                                })
                                .verifyComplete();

                verify(financialLedgerPort)
                                .commitWalletMutationAndTransaction(
                                                argThat(w -> w.id().equals(id1)
                                                                && w.balance().compareTo(new BigDecimal("95")) == 0),
                                                any(Transaction.class));
                verify(financialLedgerPort)
                                .commitWalletMutationAndTransaction(
                                                argThat(w -> w.id().equals(id2)
                                                                && w.balance().compareTo(new BigDecimal("190")) == 0),
                                                any(Transaction.class));
        }
}
