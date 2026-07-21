package com.cuscatlan.coworking.service;

import com.cuscatlan.coworking.dto.response.OccupancyResponse;
import com.cuscatlan.coworking.entity.Reservation;
import com.cuscatlan.coworking.entity.ReservationStatus;
import com.cuscatlan.coworking.entity.Space;
import com.cuscatlan.coworking.repository.ReservationRepository;
import com.cuscatlan.coworking.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    public static final String CACHE_NAME = "occupancyReport";

    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;

    @Cacheable(value = CACHE_NAME, key = "#from + '_' + #to")
    public List<OccupancyResponse> getOccupancyReport(LocalDateTime from, LocalDateTime to) {
        double totalRangeHours = Duration.between(from, to).toMinutes() / 60.0;
        if (totalRangeHours <= 0) {
            throw new IllegalArgumentException("El rango de fechas debe ser válido (from < to)");
        }

        List<Space> spaces = spaceRepository.findAll();

        return spaces.stream()
                .map(space -> buildOccupancyResponse(space, from, to, totalRangeHours))
                .toList();
    }

    private OccupancyResponse buildOccupancyResponse(Space space, LocalDateTime from, LocalDateTime to, double totalRangeHours) {
        List<Reservation> reservations = reservationRepository.findForOccupancy(
                space.getId(), ReservationStatus.CONFIRMED, from, to);

        double reservedHours = reservations.stream()
                .mapToDouble(r -> overlapHours(r, from, to))
                .sum();

        BigDecimal percentage = BigDecimal.valueOf(Math.min(reservedHours / totalRangeHours * 100, 100))
                .setScale(2, RoundingMode.HALF_UP);

        return new OccupancyResponse(space.getId(), space.getName(), percentage);
    }

    private double overlapHours(Reservation reservation, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime effectiveStart = reservation.getStartTime().isBefore(rangeStart) ? rangeStart : reservation.getStartTime();
        LocalDateTime effectiveEnd = reservation.getEndTime().isAfter(rangeEnd) ? rangeEnd : reservation.getEndTime();
        return Duration.between(effectiveStart, effectiveEnd).toMinutes() / 60.0;
    }
}