package com.yowyob.template.infrastructure.mappers;

import com.yowyob.template.domain.model.Transaction;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.TransactionRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.TransactionResponse;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversions MapStruct pour les transactions (REST ↔ domaine ↔ persistance).
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * @param request requête utilisateur
     * @return objet métier transmis au cas d’usage
     */
    Transaction toDomain(TransactionRequest request);

    /**
     * @param domain transaction métier
     * @return représentation sérialisable vers le client
     */
    TransactionResponse toResponse(Transaction domain);

    /**
     * @param response corps issu du stockage idempotence
     * @return modèle domaine
     */
    Transaction toDomain(TransactionResponse response);

    /**
     * @param domain transaction métier
     * @return entité relationnelle
     */
    @Mapping(target = "createdAt", ignore = true)
    TransactionEntity toEntity(Transaction domain);

    /**
     * @param entity ligne stockée
     * @return transaction du domaine
     */
    Transaction toDomain(TransactionEntity entity);
}
