package com.yowyob.template.domain.ports.in;

import com.yowyob.template.domain.model.Agent;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cas d’usage autour des agents : inscription, authentification et recharge déléguée au payment-service.
 */
public interface AgentUseCase {

    /**
     * Enregistre un nouvel agent si l’email est libre.
     *
     * @param agent données brutes (mot de passe en clair, haché dans le service)
     * @return agent persisté ( avec le mot de passe mais chiffré ) mais avec le mot de passe null
     * @throws RuntimeException réactive si l’email existe déjà
     */
    Mono<Agent> register(Agent agent);

    /**
     * Authentifie un agent et retourne un jeton JWT.
     *
     * @param email    identifiant de connexion
     * @param password mot de passe brut
     * @return jeton signé
     * @throws RuntimeException réactive si les identifiants sont invalides
     */
    Mono<String> login(String email, String password);

    /**
     * Publie une demande de recharge pour un portefeuille cible (événement Kafka).
     *
     * @param agentId         agent à l’origine de l’opération (journalisation possible)
     * @param targetWalletId  portefeuille à créditer côté payment-service
     * @param amount          montant positif attendu côté publisher
     * @return complétion lorsque l’événement est émis
     */
    Mono<Void> performRecharge(UUID agentId, UUID targetWalletId, BigDecimal amount);
}
