package com.payment.processing.payload;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentResponse {
    private UUID paymentId;
    private String status;
}
