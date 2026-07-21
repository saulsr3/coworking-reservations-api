package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;

class PendingPaymentState extends AbstractReservationState {

    @Override
    public ReservationStatus confirm() {
        return ReservationStatus.CONFIRMED;
    }

    @Override
    public ReservationStatus cancel() {
        return ReservationStatus.CANCELLED;
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.PENDING_PAYMENT;
    }
}