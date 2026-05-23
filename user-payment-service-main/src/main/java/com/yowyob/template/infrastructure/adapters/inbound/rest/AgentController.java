package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.model.Agent;
import com.yowyob.template.domain.ports.in.AgentUseCase;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.AuthResponse;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.LoginRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.RechargeRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * API {@code /api/v1} : inscription, authentification et recharge (Kafka).
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class AgentController {

    private final AgentUseCase agentUseCase;

    /**
     * Crée un compte agent (mot de passe haché côté service). Réponse sans mot de
     * passe.
     *
     * @param req identité et secret
     * @return agent créé sans champ {@code password}
     */
    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Agent> register(@RequestBody @Valid RegisterRequest req) {
        return agentUseCase.register(new Agent(null, req.name(), req.email(), req.password(), null))
                .map(Agent::withoutPassword);
    }

    /**
     * Retourne un jeton JWT si le couple email / mot de passe est valide.
     *
     * @param req identifiants
     * @return enveloppe contenant le token
     */
    @PostMapping("/auth/login")
    public Mono<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        return agentUseCase.login(req.email(), req.password())
                .map(AuthResponse::new);
    }

    /**
     * Publie une demande de recharge sur le bus (nécessite un JWT agent sur la
     * requête).
     *
     * @param agentId auteur de l’opération (path)
     * @param req     portefeuille cible et montant
     * @return complétion 202 lorsque l’émission Kafka est faite
     */
    @PostMapping("/agents/{agentId}/recharge")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> recharge(@PathVariable UUID agentId, @RequestBody @Valid RechargeRequest req) {
        return agentUseCase.performRecharge(agentId, req.targetWalletId(), req.amount());
    }
}
