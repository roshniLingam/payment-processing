package com.payment.processing.worker;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment.processing.component.PaymentStateMachine;
import com.payment.processing.entity.Payment;
import com.payment.processing.enums.PaymentEvent;
import com.payment.processing.enums.PaymentStatus;
import com.payment.processing.payload.PaymentCreatedEvent;
import com.payment.processing.repository.PaymentRepository;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class PaymentProcessor {
    private final PaymentRepository paymentRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events")
    @Transactional
    public void processPayment(String payload) {
        try {
            PaymentCreatedEvent paymentCreatedEvent = objectMapper.readValue(payload, PaymentCreatedEvent.class);
            Payment payment = paymentRepository.findById(paymentCreatedEvent.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
                return;
            }

            paymentStateMachine.stateTransition(payment, PaymentEvent.START_PROCESSING);

            boolean status = callExternalProvider();
            if (status) {
                paymentStateMachine.stateTransition(payment, PaymentEvent.PROCESS_SUCCESS);
            } else {
                handleFailure(payment);
            }

            paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleFailure(Payment payment) {

        payment.setRetryCount(payment.getRetryCount() + 1);

        if (payment.getRetryCount() > payment.getMaxRetries()) {
            paymentStateMachine.stateTransition(payment, PaymentEvent.RETRY_EXHAUSTED);
            return;
        }

        paymentStateMachine.stateTransition(payment, PaymentEvent.PROCESS_FAILURE);
    }

    private boolean callExternalProvider() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
