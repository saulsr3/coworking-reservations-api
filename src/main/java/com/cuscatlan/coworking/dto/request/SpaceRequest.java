package com.cuscatlan.coworking.dto.request;

import com.cuscatlan.coworking.entity.SpaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SpaceRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull SpaceType type,
        @NotNull @Positive Integer capacity,
        @NotBlank @Size(max = 200) String location,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal hourlyRate
) {
}