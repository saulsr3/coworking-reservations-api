package com.cuscatlan.coworking.dto.response;

import com.cuscatlan.coworking.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long spaceId,
        String spaceName,
        String userEmail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status,
        BigDecimal totalPrice,
        Instant createdAt
) {
}