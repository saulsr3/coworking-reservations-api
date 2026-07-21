package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;

public interface ReservationState {

    ReservationStatus confirm();

    ReservationStatus cancel();

    ReservationStatus complete();

    ReservationStatus markPendingPayment();

    ReservationStatus getStatus();
}