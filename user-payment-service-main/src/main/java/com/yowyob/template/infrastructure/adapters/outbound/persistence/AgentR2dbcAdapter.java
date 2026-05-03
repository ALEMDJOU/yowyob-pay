package com.yowyob.template.infrastructure.adapters.outbound.persistence;

import com.yowyob.template.domain.model.Agent;
import com.yowyob.template.domain.ports.out.AgentRepositoryPort;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.repository.AgentR2dbcRepository;
import com.yowyob.template.infrastructure.mappers.AgentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Implémentation R2DBC du dépôt agents.
 */
@Component
@RequiredArgsConstructor
public class AgentR2dbcAdapter implements AgentRepositoryPort {

    private final AgentR2dbcRepository repository;
    private final AgentMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Agent> save(Agent agent) {
        return repository.save(mapper.toEntity(agent))
                .map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Agent> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDomain);
    }
}
