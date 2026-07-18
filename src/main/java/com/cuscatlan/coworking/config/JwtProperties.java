package com.cuscatlan.coworking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Requisito técnico: configuración vía @ConfigurationProperties en lugar de
 * @Value disperso por las clases. Centraliza y valida la config de seguridad
 * en un solo lugar y permite bindear application-{profile}.yml de forma tipada.
 */
@ConfigurationProperties(prefix = "app.jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    @NotBlank
    private String secret;

    @Positive
    private long expirationMs;
}
