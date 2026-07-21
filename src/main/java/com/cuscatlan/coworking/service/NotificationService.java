package com.cuscatlan.coworking.service;

import com.cuscatlan.coworking.entity.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Async("notificationExecutor")
    public void sendReservationConfirmation(Reservation reservation) {
        // Simulación de latencia de un proveedor de correo real,
        // para demostrar que el hilo HTTP principal no se bloquea con esto.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[NOTIFICACION-ASYNC] Correo de confirmación enviado a {} para la reserva #{} del espacio '{}' ({} - {})",
                reservation.getUser().getEmail(),
                reservation.getId(),
                reservation.getSpace().getName(),
                reservation.getStartTime(),
                reservation.getEndTime());
    }
}