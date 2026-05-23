package com.yowyob.template.application.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yowyob.template.domain.model.Agent;
import com.yowyob.template.domain.ports.in.AgentUseCase;
import com.yowyob.template.domain.ports.out.AgentRepositoryPort;
import com.yowyob.template.domain.ports.out.RechargePublisherPort;
import com.yowyob.template.infrastructure.config.UserBusinessProperties;
import com.yowyob.template.infrastructure.security.JwtService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Implémentation des cas d’usage agents : inscription, login JWT, recharge via
 * Kafka.
 */
@Service
@RequiredArgsConstructor
public class AgentService implements AgentUseCase {

    private final AgentRepositoryPort repository;
    private final RechargePublisherPort rechargePublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserBusinessProperties userBusinessProperties;

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException si l’email est déjà enregistré
     */
    @Override
    public Mono<Agent> register(Agent agent) {
        return repository.findByEmail(agent.email())
                .flatMap(existing -> Mono.<Agent>error(new RuntimeException("Email déjà utilisé")))
                .switchIfEmpty(Mono.defer(() -> {
                    String encodedPwd = passwordEncoder.encode(agent.password());
                    Agent newAgent = new Agent(null, agent.name(), agent.email(), encodedPwd, "ACTIVE");
                    return repository.save(newAgent);
                }));
    }

    /**
     * Inscription avec émission immédiate d’un JWT (même claims que
     * {@link #login}).
     */
    public Mono<String> registerAndIssueToken(Agent agent) {
        return register(agent)
                .map(saved -> jwtService.generateToken(saved.email(), saved.id(), saved.name()));
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException en cas d’échec d’authentification
     */
    @Override
    public Mono<String> login(String email, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))
                .flatMap(auth -> repository.findByEmail(auth.getName())
                        .switchIfEmpty(Mono.error(new RuntimeException("Agent introuvable")))
                        .map(agent -> jwtService.generateToken(agent.email(), agent.id(), agent.name())))
                .onErrorMap(e -> new RuntimeException("Identifiants incorrects"));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException si le montant dépasse
     *                                  {@link UserBusinessProperties#maxRechargeAmount()}
     */
    @Override
    public Mono<Void> performRecharge(UUID agentId, UUID targetWalletId, BigDecimal amount) {
        BigDecimal max = userBusinessProperties.maxRechargeAmount();
        if (amount != null && amount.compareTo(max) > 0) {
            return Mono.error(new IllegalArgumentException(
                    "Montant supérieur au plafond autorisé pour une recharge (" + max + ")"));
        }

        return rechargePublisher.publishRechargeEvent(targetWalletId, amount);
    }
}
