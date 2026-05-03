package com.yowyob.template.infrastructure.mappers;

import com.yowyob.template.domain.model.Agent;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.RegisterRequest;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.AgentEntity;
import org.mapstruct.Mapper;

/**
 * Conversions entre DTO d’inscription, entité SQL et modèle {@link Agent}.
 */
@Mapper(componentModel = "spring")
public interface AgentMapper {

    /**
     * @param request données saisies à l’inscription
     * @return agent domaine (mot de passe en clair)
     */
    Agent toDomain(RegisterRequest request);

    /**
     * @param domain agent métier
     * @return ligne relationnelle
     */
    AgentEntity toEntity(Agent domain);

    /**
     * @param entity ligne {@code agents}
     * @return modèle riche
     */
    Agent toDomain(AgentEntity entity);
}
