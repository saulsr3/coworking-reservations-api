package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;

class CancelledState extends AbstractReservationState {

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.CANCELLED;
    }
}