package com.cuscatlan.coworking.dto.response;

public class PaymentValidationResult {

    private final boolean approved;
    private final String message;

    public PaymentValidationResult(boolean approved, String message) {
        this.approved = approved;
        this.message = message;
    }

    public static PaymentValidationResult success() {
        return new PaymentValidationResult(true, "Pago validado correctamente");
    }

    public static PaymentValidationResult rejected(String reason) {
        return new PaymentValidationResult(false, reason);
    }

    public boolean approved() {
        return approved;
    }

    public String message() {
        return message;
    }
}