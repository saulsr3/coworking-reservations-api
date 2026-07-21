package com.cuscatlan.coworking.dto.response;

import java.math.BigDecimal;

public record OccupancyResponse(
        Long spaceId,
        String spaceName,
        BigDecimal occupancyPercentage
) {
}