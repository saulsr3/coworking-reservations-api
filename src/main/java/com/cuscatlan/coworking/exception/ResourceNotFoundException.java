package com.cuscatlan.coworking.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String entity, Object id) {
        super(entity + " no encontrado con id: " + id, HttpStatus.NOT_FOUND);
    }
}
