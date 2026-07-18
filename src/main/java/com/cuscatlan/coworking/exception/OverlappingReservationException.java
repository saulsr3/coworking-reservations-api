package com.cuscatlan.coworking.exception;

import org.springframework.http.HttpStatus;

public class OverlappingReservationException extends BusinessException {
    public OverlappingReservationException(Long spaceId) {
        super("El espacio " + spaceId + " ya tiene una reserva en ese horario", HttpStatus.CONFLICT);
    }
}
