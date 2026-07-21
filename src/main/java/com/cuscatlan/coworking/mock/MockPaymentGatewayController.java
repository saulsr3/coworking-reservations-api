package com.cuscatlan.coworking.mock;


import com.cuscatlan.coworking.dto.response.PaymentValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@Slf4j
public class MockPaymentGatewayController {

    @PostMapping("/mock/payment-gateway/validate")
    public ResponseEntity<PaymentValidationResult> validate(@RequestParam String reservationId) throws InterruptedException {
        long delayMs = ThreadLocalRandom.current().nextLong(200, 2500);
        Thread.sleep(delayMs);

        boolean shouldFail = ThreadLocalRandom.current().nextInt(100) < 35;

        if (shouldFail) {
            log.warn("[MOCK-PAYMENT-GATEWAY] Fallo simulado para reserva {} (latencia {}ms)", reservationId, delayMs);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(PaymentValidationResult.rejected("Pasarela de pago no disponible temporalmente"));
        }

        log.info("[MOCK-PAYMENT-GATEWAY] Pago aprobado para reserva {} (latencia {}ms)", reservationId, delayMs);
        return ResponseEntity.ok(PaymentValidationResult.success());
    }
}