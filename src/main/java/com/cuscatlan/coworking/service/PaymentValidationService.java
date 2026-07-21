package com.cuscatlan.coworking.service;

import com.cuscatlan.coworking.client.PaymentGatewayClient;
import com.cuscatlan.coworking.dto.response.PaymentValidationResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentValidationService {

    private final PaymentGatewayClient paymentGatewayClient;

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallback")
    public PaymentValidationResult validate(Long reservationId) {
        return paymentGatewayClient.validate(reservationId);
    }

    private PaymentValidationResult fallback(Long reservationId, Throwable throwable) {
        log.warn("[CIRCUIT-BREAKER] Fallback activado para reserva {} - causa: {}",
                reservationId, throwable.getClass().getSimpleName());
        return PaymentValidationResult.rejected(
                "Servicio de pago no disponible en este momento, la reserva quedará pendiente de pago");
    }
}