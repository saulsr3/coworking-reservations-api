package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;

public final class ReservationStateFactory {

    private static final PendingState PENDING = new PendingState();
    private static final PendingPaymentState PENDING_PAYMENT = new PendingPaymentState();
    private static final ConfirmedState CONFIRMED = new ConfirmedState();
    private static final CancelledState CANCELLED = new CancelledState();
    private static final CompletedState COMPLETED = new CompletedState();

    private ReservationStateFactory() {
    }

    public static ReservationState forStatus(ReservationStatus status) {
        return switch (status) {
            case PENDING -> PENDING;
            case PENDING_PAYMENT -> PENDING_PAYMENT;
            case CONFIRMED -> CONFIRMED;
            case CANCELLED -> CANCELLED;
            case COMPLETED -> COMPLETED;
        };
    }
}