package com.payment.processing.component;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.payment.processing.entity.Payment;
import com.payment.processing.enums.PaymentEvent;
import com.payment.processing.enums.PaymentStatus;

@Component
public class PaymentStateMachine {
    private static final Map<PaymentStatus, Map<PaymentEvent, PaymentStatus>> transitionsMap =
        Map.of(
            PaymentStatus.PENDING, Map.of(
                PaymentEvent.START_PROCESSING, PaymentStatus.PROCESSING,
                PaymentEvent.RETRY_EXHAUSTED, PaymentStatus.FAILED
            ),
            PaymentStatus.PROCESSING, Map.of(
                PaymentEvent.PROCESS_SUCCESS, PaymentStatus.SUCCESS,
                PaymentEvent.PROCESS_FAILURE, PaymentStatus.PENDING
            ),
            PaymentStatus.SUCCESS, Map.of(),
            PaymentStatus.FAILED, Map.of()
        );

        public void stateTransition(Payment payment, PaymentEvent paymentEvent) {
            PaymentStatus currentPaymentStatus = payment.getStatus();
            Map<PaymentEvent, PaymentStatus> allowedTransition = 
                        transitionsMap.getOrDefault(currentPaymentStatus, Map.of());
            if(!allowedTransition.containsKey(paymentEvent)){
                throw new IllegalStateException("Invalid transition: " + currentPaymentStatus + " -> " + paymentEvent);
            }

            payment.setStatus(allowedTransition.get(paymentEvent));
        }
}
