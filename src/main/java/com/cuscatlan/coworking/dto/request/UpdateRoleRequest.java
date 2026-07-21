package com.cuscatlan.coworking.dto.request;

import com.cuscatlan.coworking.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull Role role
) {
}