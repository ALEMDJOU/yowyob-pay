package com.yowyob.template.infrastructure.mappers;


import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.WalletRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.WalletResponse;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.WalletEntity;
import org.mapstruct.Mapper;

/**
 * Conversions MapStruct entre DTO REST, entité R2DBC et modèle de domaine {@link Wallet}.
 */
@Mapper(componentModel = "spring")
public interface WalletMapper {

    /**
     * @param request corps de requête API
     * @return modèle de domaine prêt pour le service
     */
    Wallet toDomain(WalletRequest request);

    /**
     * @param domain entité métier
     * @return payload de réponse API
     */
    WalletResponse toResponse(Wallet domain);

    /**
     * @param domain modèle de domaine
     * @return entité persistée
     */
    WalletEntity toEntity(Wallet domain);

    /**
     * @param entity ligne base de données
     * @return modèle de domaine
     */
    Wallet toDomain(WalletEntity entity);
}
