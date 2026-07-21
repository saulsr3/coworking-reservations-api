package com.cuscatlan.coworking.dto.response;

import com.cuscatlan.coworking.entity.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role
) {
}