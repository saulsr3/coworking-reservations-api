package com.cuscatlan.coworking.repository;

import com.cuscatlan.coworking.entity.Reservation;
import com.cuscatlan.coworking.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Bloquea las filas candidatas a solapamiento con PESSIMISTIC_WRITE dentro
     * de la transacción de creación de reserva, de modo que dos solicitudes
     * concurrentes para el mismo espacio/horario no puedan pasar ambas la
     * validación antes de que la primera haga commit (evita condición de carrera
     * que un simple SELECT + INSERT no cubriría).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.space.id = :spaceId
              AND r.status <> com.cuscatlan.coworking.entity.ReservationStatus.CANCELLED
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    List<Reservation> findOverlapping(@Param("spaceId") Long spaceId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.space
            JOIN FETCH r.user
            WHERE r.space.id = :spaceId
              AND r.status = :status
              AND r.startTime < :rangeEnd
              AND r.endTime > :rangeStart
            """)
    List<Reservation> findForOccupancy(@Param("spaceId") Long spaceId,
                                        @Param("status") ReservationStatus status,
                                        @Param("rangeStart") LocalDateTime rangeStart,
                                        @Param("rangeEnd") LocalDateTime rangeEnd);
}
