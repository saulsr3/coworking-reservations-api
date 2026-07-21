package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;
import com.cuscatlan.coworking.exception.InvalidReservationStateException;

abstract class AbstractReservationState implements ReservationState {

    @Override
    public ReservationStatus confirm() {
        throw invalidTransition("CONFIRMED");
    }

    @Override
    public ReservationStatus cancel() {
        throw invalidTransition("CANCELLED");
    }

    @Override
    public ReservationStatus complete() {
        throw invalidTransition("COMPLETED");
    }

    @Override
    public ReservationStatus markPendingPayment() {
        throw invalidTransition("PENDING_PAYMENT");
    }

    protected InvalidReservationStateException invalidTransition(String target) {
        return new InvalidReservationStateException(
                "No se puede pasar de " + getStatus() + " a " + target);
    }
}