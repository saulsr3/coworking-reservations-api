package com.cuscatlan.coworking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * El registro público NUNCA acepta un rol desde el cliente: siempre crea USER.
 * Un ADMIN existente promueve a otros usuarios via /api/users/{id}/role
 * (protegido con hasRole("ADMIN")). Esto evita que cualquiera se autoasigne
 * privilegios de administrador en el payload de registro.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 120)
        String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password
) {
}
