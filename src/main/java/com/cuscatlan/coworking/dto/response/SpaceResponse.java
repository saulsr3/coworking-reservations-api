package com.cuscatlan.coworking.dto.response;

import com.cuscatlan.coworking.entity.SpaceType;

import java.math.BigDecimal;
import java.time.Instant;

public record SpaceResponse(
        Long id,
        String name,
        SpaceType type,
        Integer capacity,
        String location,
        BigDecimal hourlyRate,
        Instant createdAt
) {
}