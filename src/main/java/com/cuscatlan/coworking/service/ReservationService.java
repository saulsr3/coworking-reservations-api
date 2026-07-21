package com.cuscatlan.coworking.service;

import com.cuscatlan.coworking.dto.request.ReservationRequest;
import com.cuscatlan.coworking.dto.response.PaymentValidationResult;
import com.cuscatlan.coworking.dto.response.ReservationResponse;
import com.cuscatlan.coworking.entity.Reservation;
import com.cuscatlan.coworking.entity.ReservationStatus;
import com.cuscatlan.coworking.entity.Space;
import com.cuscatlan.coworking.entity.User;
import com.cuscatlan.coworking.exception.OverlappingReservationException;
import com.cuscatlan.coworking.exception.ResourceNotFoundException;
import com.cuscatlan.coworking.mapper.ReservationMapper;
import com.cuscatlan.coworking.repository.ReservationRepository;
import com.cuscatlan.coworking.repository.UserRepository;
import com.cuscatlan.coworking.state.ReservationState;
import com.cuscatlan.coworking.state.ReservationStateFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceService spaceService;
    private final ReservationMapper reservationMapper;
    private final NotificationService notificationService;
    private final PaymentValidationService paymentValidationService;

    @Transactional
    @CacheEvict(value = ReportService.CACHE_NAME, allEntries = true)
    public ReservationResponse create(ReservationRequest request, String userEmail) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Space space = spaceService.getSpaceOrThrow(request.spaceId());

        List<Reservation> overlapping = reservationRepository.findOverlapping(
                space.getId(), request.startTime(), request.endTime());
        if (!overlapping.isEmpty()) {
            throw new OverlappingReservationException(space.getId());
        }

        BigDecimal totalPrice = calculatePrice(space.getHourlyRate(), request.startTime(), request.endTime());

        Reservation reservation = Reservation.builder()
                .user(user)
                .space(space)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(ReservationStatus.PENDING)
                .totalPrice(totalPrice)
                .build();

        reservation = reservationRepository.save(reservation);

        PaymentValidationResult paymentResult = paymentValidationService.validate(reservation.getId());
        if (paymentResult.approved()) {
            applyTransition(reservation, ReservationState::confirm);
            notificationService.sendReservationConfirmation(reservation);
        } else {
            applyTransition(reservation, ReservationState::markPendingPayment);
        }

        return reservationMapper.toResponse(reservation);
    }

    public List<ReservationResponse> findMine(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        return reservationRepository.findByUserIdOrderByStartTimeDesc(user.getId())
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = ReportService.CACHE_NAME, allEntries = true)
    public ReservationResponse cancel(Long id, String userEmail, boolean isAdmin) {
        Reservation reservation = getReservationOrThrow(id);

        if (!isAdmin && !reservation.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("No puedes cancelar una reserva que no es tuya");
        }

        applyTransition(reservation, ReservationState::cancel);
        return reservationMapper.toResponse(reservation);
    }

    private void applyTransition(Reservation reservation, Function<ReservationState, ReservationStatus> transition) {
        ReservationState currentState = ReservationStateFactory.forStatus(reservation.getStatus());
        ReservationStatus nextStatus = transition.apply(currentState);
        reservation.setStatus(nextStatus);
    }

    private Reservation getReservationOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private BigDecimal calculatePrice(BigDecimal hourlyRate, LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return hourlyRate.multiply(hours);
    }
}