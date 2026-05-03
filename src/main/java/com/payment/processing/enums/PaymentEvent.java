package com.payment.processing.enums;

public enum PaymentEvent {
    CREATE,
    START_PROCESSING,
    PROCESS_SUCCESS,
    PROCESS_FAILURE,
    RETRY_EXHAUSTED
}
