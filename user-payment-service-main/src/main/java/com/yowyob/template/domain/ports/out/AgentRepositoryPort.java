package com.yowyob.template.domain.ports.out;

import com.yowyob.template.domain.model.Agent;
import reactor.core.publisher.Mono;

/**
 * Persistance des agents (ex. PostgreSQL via R2DBC).
 */
public interface AgentRepositoryPort {

    /**
     * @param agent agent à insérer ou mettre à jour
     * @return agent après sauvegarde
     */
    Mono<Agent> save(Agent agent);

    /**
     * @param email adresse unique
     * @return agent trouvé ou vide
     */
    Mono<Agent> findByEmail(String email);
}
