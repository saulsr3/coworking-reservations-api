package com.cuscatlan.coworking.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends BusinessException {
    public EmailAlreadyInUseException(String email) {
        super("El correo ya está registrado: " + email, HttpStatus.CONFLICT);
    }
}
