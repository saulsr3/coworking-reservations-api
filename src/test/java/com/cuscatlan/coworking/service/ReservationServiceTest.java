package com.cuscatlan.coworking.service;

import com.cuscatlan.coworking.dto.request.ReservationRequest;
import com.cuscatlan.coworking.dto.response.PaymentValidationResult;
import com.cuscatlan.coworking.dto.response.ReservationResponse;
import com.cuscatlan.coworking.entity.*;
import com.cuscatlan.coworking.exception.OverlappingReservationException;
import com.cuscatlan.coworking.exception.InvalidReservationStateException;
import com.cuscatlan.coworking.mapper.ReservationMapper;
import com.cuscatlan.coworking.repository.ReservationRepository;
import com.cuscatlan.coworking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SpaceService spaceService;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentValidationService paymentValidationService;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Space space;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("saul@example.com").role(Role.USER).build();
        space = Space.builder().id(1L).name("Sala A").hourlyRate(BigDecimal.valueOf(10)).build();
    }

    @Test
    void create_shouldThrowOverlappingReservationException_whenOverlapExists() {
        ReservationRequest request = new ReservationRequest(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2));

        when(userRepository.findByEmail("saul@example.com")).thenReturn(Optional.of(user));
        when(spaceService.getSpaceOrThrow(1L)).thenReturn(space);
        when(reservationRepository.findOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of(new Reservation())); // ya existe algo que se solapa

        assertThatThrownBy(() -> reservationService.create(request, "saul@example.com"))
                .isInstanceOf(OverlappingReservationException.class);

        verify(reservationRepository, never()).save(any());
        verifyNoInteractions(paymentValidationService);
    }

    @Test
    void create_shouldConfirmReservation_whenPaymentApproved() {
        ReservationRequest request = new ReservationRequest(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2));

        Reservation savedReservation = Reservation.builder()
                .id(99L).user(user).space(space)
                .startTime(request.startTime()).endTime(request.endTime())
                .status(ReservationStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(20))
                .build();

        when(userRepository.findByEmail("saul@example.com")).thenReturn(Optional.of(user));
        when(spaceService.getSpaceOrThrow(1L)).thenReturn(space);
        when(reservationRepository.findOverlapping(anyLong(), any(), any())).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(paymentValidationService.validate(99L)).thenReturn(PaymentValidationResult.success());
        when(reservationMapper.toResponse(any())).thenReturn(
                new ReservationResponse(99L, 1L, "Sala A", "saul@example.com",
                        request.startTime(), request.endTime(), ReservationStatus.CONFIRMED,
                        BigDecimal.valueOf(20), null));

        ReservationResponse response = reservationService.create(request, "saul@example.com");

        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
        // Confirma que la notificación async SÍ se dispara cuando el pago se aprueba.
        verify(notificationService, times(1)).sendReservationConfirmation(any());
    }

    @Test
    void create_shouldMarkPendingPayment_whenPaymentRejected() {
        ReservationRequest request = new ReservationRequest(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2));

        Reservation savedReservation = Reservation.builder()
                .id(100L).user(user).space(space)
                .startTime(request.startTime()).endTime(request.endTime())
                .status(ReservationStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(20))
                .build();

        when(userRepository.findByEmail("saul@example.com")).thenReturn(Optional.of(user));
        when(spaceService.getSpaceOrThrow(1L)).thenReturn(space);
        when(reservationRepository.findOverlapping(anyLong(), any(), any())).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(paymentValidationService.validate(100L)).thenReturn(PaymentValidationResult.rejected("gateway caído"));
        when(reservationMapper.toResponse(any())).thenReturn(
                new ReservationResponse(100L, 1L, "Sala A", "saul@example.com",
                        request.startTime(), request.endTime(), ReservationStatus.PENDING_PAYMENT,
                        BigDecimal.valueOf(20), null));

        ReservationResponse response = reservationService.create(request, "saul@example.com");

        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        // Si el pago se rechaza, NO debe dispararse la notificación de confirmación.
        verify(notificationService, never()).sendReservationConfirmation(any());
    }

    @Test
    void cancel_shouldThrowAccessDenied_whenNotOwnerAndNotAdmin() {
        Reservation reservation = Reservation.builder()
                .id(5L).user(user).status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(5L, "otro-usuario@example.com", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancel_shouldSucceed_whenAdminCancelsSomeoneElsesReservation() {
        Reservation reservation = Reservation.builder()
                .id(5L).user(user).status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(any())).thenReturn(
                new ReservationResponse(5L, 1L, "Sala A", "saul@example.com",
                        null, null, ReservationStatus.CANCELLED, BigDecimal.TEN, null));

        ReservationResponse response = reservationService.cancel(5L, "admin@example.com", true);

        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancel_shouldThrowInvalidState_whenAlreadyCancelled() {
        Reservation reservation = Reservation.builder()
                .id(5L).user(user).status(ReservationStatus.CANCELLED) // ya cancelada
                .build();

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(5L, "saul@example.com", false))
                .isInstanceOf(InvalidReservationStateException.class);
    }
}
