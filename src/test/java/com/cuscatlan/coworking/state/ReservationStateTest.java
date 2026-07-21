package com.cuscatlan.coworking.state;

import com.cuscatlan.coworking.entity.ReservationStatus;
import com.cuscatlan.coworking.exception.InvalidReservationStateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationStateTest {

    @Test
    void pendingState_shouldAllowConfirmCancelAndPendingPayment() {
        ReservationState state = ReservationStateFactory.forStatus(ReservationStatus.PENDING);

        assertThat(state.confirm()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(state.cancel()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(state.markPendingPayment()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
    }

    @Test
    void pendingState_shouldRejectComplete() {
        ReservationState state = ReservationStateFactory.forStatus(ReservationStatus.PENDING);

        assertThatThrownBy(state::complete)
                .isInstanceOf(InvalidReservationStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("COMPLETED");
    }

    @Test
    void confirmedState_shouldAllowCancelAndComplete() {
        ReservationState state = ReservationStateFactory.forStatus(ReservationStatus.CONFIRMED);

        assertThat(state.cancel()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void confirmedState_shouldRejectConfirmAgain() {
        ReservationState state = ReservationStateFactory.forStatus(ReservationStatus.CONFIRMED);

        assertThatThrownBy(state::confirm)
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void cancelledState_isTerminal_rejectsAllTransitions() {
        ReservationState state = ReservationStateFactory.forStatus(ReservationStatus.CANCELLED);

        assertThatThrownBy(state::confirm).isInstanceOf(InvalidReservationStateException.class);
        assertThatThrownBy(state::cancel).isInstanceOf(InvalidReservationStateException.class);
        assertThatThrownBy(state::complete).isInstanceOf(InvalidReservationStateException.class);
        assertThatThrownBy(state::markPendingPayment).isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void completedState_isTerminal_rejectsAllTransitions() {
        ReservationState state = ReservationStateFactory.forStatus(ReservationStatus.COMPLETED);

        assertThatThrownBy(state::confirm).isInstanceOf(InvalidReservationStateException.class);
        assertThatThrownBy(state::cancel).isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void pendingPaymentState_shouldAllowConfirmAndCancel_butNotComplete() {
        ReservationState state = ReservationStateFactory.forStatus(ReservationStatus.PENDING_PAYMENT);

        assertThat(state.confirm()).isEqualTo(ReservationStatus.CONFIRMED);

        ReservationState stateForCancel = ReservationStateFactory.forStatus(ReservationStatus.PENDING_PAYMENT);
        assertThat(stateForCancel.cancel()).isEqualTo(ReservationStatus.CANCELLED);

        ReservationState stateForComplete = ReservationStateFactory.forStatus(ReservationStatus.PENDING_PAYMENT);
        assertThatThrownBy(stateForComplete::complete).isInstanceOf(InvalidReservationStateException.class);
    }
}