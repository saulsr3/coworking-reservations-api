package com.cuscatlan.coworking.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.payment-gateway")
@Validated
@Getter
@Setter
public class PaymentGatewayProperties {

    @NotBlank
    private String baseUrl;

    @Positive
    private long timeoutMs;
}