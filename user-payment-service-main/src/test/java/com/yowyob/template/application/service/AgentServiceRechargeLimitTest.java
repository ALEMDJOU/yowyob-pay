package com.yowyob.template.application.service;

import com.yowyob.template.domain.ports.out.AgentRepositoryPort;
import com.yowyob.template.domain.ports.out.RechargePublisherPort;
import com.yowyob.template.infrastructure.config.UserBusinessProperties;
import com.yowyob.template.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Nom : {@code AgentServiceRechargeLimitTest}
 * <p>
 * Description : vérifie le plafond métier sur
 * {@link AgentService#performRecharge(java.util.UUID, java.util.UUID, java.math.BigDecimal)}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AgentServiceRechargeLimitTest {

        @Mock
        private AgentRepositoryPort repository;

        @Mock
        private RechargePublisherPort rechargePublisher;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtService jwtService;

        @Mock
        private ReactiveAuthenticationManager authenticationManager;

        /**
         * Nom : {@code performRecharge_aboveMax_emitsIllegalArgument}
         * <p>
         * Description : un montant strictement supérieur au plafond ne doit pas appeler
         * Kafka.
         * </p>
         */
        @Test
        @DisplayName("Recharge > plafond → erreur et aucun publish Kafka")
        void performRecharge_aboveMax_emitsIllegalArgument() {
                UserBusinessProperties limits = new UserBusinessProperties(new BigDecimal("100"));
                AgentService service = new AgentService(
                                repository,
                                rechargePublisher,
                                passwordEncoder,
                                jwtService,
                                authenticationManager,
                                limits);

                StepVerifier.create(
                                service.performRecharge(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("101")))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verifyNoInteractions(rechargePublisher);
        }

        /**
         * Nom : {@code performRecharge_atMax_callsPublisher}
         * <p>
         * Description : un montant égal au plafond doit être accepté et délégué au
         * publisher.
         * </p>
         */
        @Test
        @DisplayName("Recharge = plafond → publish Kafka")
        void performRecharge_atMax_callsPublisher() {
                UserBusinessProperties limits = new UserBusinessProperties(new BigDecimal("100"));
                AgentService service = new AgentService(
                                repository,
                                rechargePublisher,
                                passwordEncoder,
                                jwtService,
                                authenticationManager,
                                limits);
                UUID wallet = UUID.randomUUID();
                when(rechargePublisher.publishRechargeEvent(eq(wallet),
                                argThat(a -> a.compareTo(new BigDecimal("100")) == 0)))
                                .thenReturn(Mono.empty());

                StepVerifier.create(service.performRecharge(UUID.randomUUID(), wallet, new BigDecimal("100")))
                                .verifyComplete();

                verify(rechargePublisher).publishRechargeEvent(eq(wallet),
                                argThat(a -> a.compareTo(new BigDecimal("100")) == 0));
        }
}
