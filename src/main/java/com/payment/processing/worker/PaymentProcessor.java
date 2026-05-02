package com.payment.processing.worker;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.payment.processing.repository.PaymentRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PaymentProcessor {
    private final PaymentRepository paymentRepository;

    @KafkaListener(topics = "payment-events")
    public void processPayment(String payload) {
        // parse payload
        // call external payment provider
        // update DB
    }
}
