package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;

class CompletedState extends AbstractReservationState {

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.COMPLETED;
    }
}