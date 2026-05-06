package com.payment.processing.service;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment.processing.component.PaymentStateMachine;
import com.payment.processing.entity.Payment;
import com.payment.processing.enums.PaymentEvent;
import com.payment.processing.enums.PaymentStatus;
import com.payment.processing.repository.PaymentRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PaymentExecutionService {
    private final PaymentRepository paymentRepository;
    private final PaymentStateMachine paymentStateMachine;

    private static final long BASE_DELAY_MS = 5000;
    private static final long MAX_DELAY_MS = 5 * 60 * 1000;

    @Transactional
    public void process(Payment payment){
        if (isTerminal(payment)) return;

        // prevent premature retry
        if (payment.getNextRetryAt() != null &&
            payment.getNextRetryAt().isAfter(Instant.now())) {
            return;
        }

        payment.setLastAttemptAt(Instant.now());
        paymentStateMachine.stateTransition(payment, PaymentEvent.START_PROCESSING);

        try {
            boolean success = callExternalProvider();

            if (success) {
                paymentStateMachine.stateTransition(payment, PaymentEvent.PROCESS_SUCCESS);
                clearRetryFields(payment);
            } else {
                handleFailure(payment, "Provider returned failure");
            }

        } catch (Exception ex) {
            handleFailure(payment, ex.getMessage());
        }

        paymentRepository.save(payment);
    }

    private void handleFailure(Payment payment, String error) {

        int retryCount = payment.getRetryCount() + 1;
        payment.setRetryCount(retryCount);
        payment.setLastError(error);

        if (retryCount > payment.getMaxRetries()) {
            paymentStateMachine.stateTransition(payment, PaymentEvent.RETRY_EXHAUSTED);
            return;
        }

        long delay = (long) (BASE_DELAY_MS * Math.pow(2, retryCount - 1));

        // cap delay
        delay = Math.min(delay, MAX_DELAY_MS);

        // jitter
        delay += ThreadLocalRandom.current().nextLong(1000);
        
        payment.setNextRetryAt(Instant.now().plusMillis(delay));

        paymentStateMachine.stateTransition(payment, PaymentEvent.PROCESS_FAILURE);
    }

    private void clearRetryFields(Payment payment) {
        payment.setRetryCount(0);
        payment.setNextRetryAt(null);
        payment.setLastError(null);
    }

    private boolean isTerminal(Payment payment) {
        return payment.getStatus() == PaymentStatus.SUCCESS ||
               payment.getStatus() == PaymentStatus.FAILED;
    }

    private boolean callExternalProvider() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
