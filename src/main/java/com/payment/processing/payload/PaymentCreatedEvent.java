package com.payment.processing.payload;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentCreatedEvent {
    private UUID paymentId;
    private String userId;
    private Long amount;
}
