package com.cuscatlan.coworking.mapper;

import com.cuscatlan.coworking.dto.response.ReservationResponse;
import com.cuscatlan.coworking.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "space.id", target = "spaceId")
    @Mapping(source = "space.name", target = "spaceName")
    @Mapping(source = "user.email", target = "userEmail")
    ReservationResponse toResponse(Reservation reservation);
}