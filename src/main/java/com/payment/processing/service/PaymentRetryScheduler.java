package com.payment.processing.service;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment.processing.entity.Payment;
import com.payment.processing.repository.PaymentRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PaymentRetryScheduler {
    private final PaymentRepository paymentRepository;
    private final PaymentExecutionService processingService;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void retryPayments() {

        List<Payment> payments =
            paymentRepository.findRetryablePayments(Instant.now());

        for (Payment payment : payments) {
            processingService.process(payment);
        }
    }
}
