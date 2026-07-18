package com.cuscatlan.coworking.exception;

import org.springframework.http.HttpStatus;

public class InvalidReservationStateException extends BusinessException {
    public InvalidReservationStateException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
