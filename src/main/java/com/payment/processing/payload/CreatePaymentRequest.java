package com.payment.processing.payload;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentRequest {
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String providerReference;
}
