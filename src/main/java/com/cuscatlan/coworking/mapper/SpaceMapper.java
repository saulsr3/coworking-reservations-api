package com.cuscatlan.coworking.mapper;

import com.cuscatlan.coworking.dto.request.SpaceRequest;
import com.cuscatlan.coworking.dto.response.SpaceResponse;
import com.cuscatlan.coworking.entity.Space;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct genera la implementación de esta interfaz en tiempo de compilación
 * (revisa target/generated-sources tras compilar). Evita escribir a mano
 * space.setName(dto.name()), etc., para cada campo.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SpaceMapper {

    Space toEntity(SpaceRequest request);

    SpaceResponse toResponse(Space space);

    /**
     * Actualiza una entidad existente en lugar de crear una nueva, preservando
     * el id y createdAt. @MappingTarget le dice a MapStruct "modifica este
     * objeto en vez de construir uno nuevo".
     */
    void updateEntityFromRequest(SpaceRequest request, @MappingTarget Space space);
}