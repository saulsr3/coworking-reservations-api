package com.cuscatlan.coworking.client;

import com.cuscatlan.coworking.dto.response.PaymentValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PaymentGatewayClient {

    private final WebClient paymentGatewayWebClient;

    public PaymentValidationResult validate(Long reservationId) {
        return paymentGatewayWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/mock/payment-gateway/validate")
                        .queryParam("reservationId", reservationId)
                        .build())
                .retrieve()
                .bodyToMono(PaymentValidationResult.class)
                .block();
    }
}