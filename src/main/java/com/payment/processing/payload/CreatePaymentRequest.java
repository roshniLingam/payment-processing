package com.payment.processing.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentRequest {
    private String userId;
    private Long amount;
    private String currency;
    private String paymentMethod;
}
