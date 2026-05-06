package com.payment.processing.worker;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment.processing.entity.Payment;
import com.payment.processing.payload.PaymentCreatedEvent;
import com.payment.processing.repository.PaymentRepository;
import com.payment.processing.service.PaymentExecutionService;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class PaymentEventConsumer {
    private final PaymentRepository paymentRepository;
    private final PaymentExecutionService processingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events")
    @Transactional
    public void processPayment(String payload) {

        try {
            PaymentCreatedEvent event =
                objectMapper.readValue(payload, PaymentCreatedEvent.class);

            Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

            processingService.process(payment);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
