package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;

class ConfirmedState extends AbstractReservationState {

    @Override
    public ReservationStatus cancel() {
        return ReservationStatus.CANCELLED;
    }

    @Override
    public ReservationStatus complete() {
        return ReservationStatus.COMPLETED;
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.CONFIRMED;
    }
}