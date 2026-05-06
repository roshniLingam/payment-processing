package com.payment.processing.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment.processing.entity.OutboxEvent;
import com.payment.processing.entity.Payment;
import com.payment.processing.enums.EventType;
import com.payment.processing.enums.OutboxStatus;
import com.payment.processing.enums.PaymentStatus;
import com.payment.processing.payload.CreatePaymentRequest;
import com.payment.processing.payload.PaymentCreatedEvent;
import com.payment.processing.payload.PaymentResponse;
import com.payment.processing.repository.OutboxEventRepository;
import com.payment.processing.repository.PaymentRepository;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;


@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private static final String AGGREGATE_TYPE = "Payment";

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, String idempotencyKey) {
        try {
            return processNewPayment(request, idempotencyKey);
        } catch (DataIntegrityViolationException ex) {
            // idempotency fallback
            Payment existing = paymentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new RuntimeException("Payment exists but not found"));

            return mapToResponse(existing);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment Id not found"));
        return mapToResponse(payment);
    }
 
    private PaymentResponse processNewPayment(CreatePaymentRequest request, String idempotencyKey) {
        Payment payment = buildPayment(request, idempotencyKey);
        paymentRepository.save(payment);

        OutboxEvent outboxEvent = buildOutboxEvent(payment);
        outboxEventRepository.save(outboxEvent);

        return mapToResponse(payment);
    }

    private Payment buildPayment(CreatePaymentRequest request, String idempotencyKey) {
        Payment payment = new Payment();
        payment.setIdempotencyKey(idempotencyKey);
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProviderReference(request.getProviderReference());
        return payment;
    }

    private OutboxEvent buildOutboxEvent(Payment payment) {
        try {
            PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(payment.getId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .build();

            OutboxEvent outbox = new OutboxEvent();
            outbox.setAggregateType(AGGREGATE_TYPE);
            outbox.setAggregateId(payment.getId().toString());
            outbox.setEventType(EventType.PAYMENT_CREATED);
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outbox.setStatus(OutboxStatus.NEW);
            outbox.setCreatedAt(Instant.now());

            return outbox;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
            .paymentId(payment.getId())
            .status(payment.getStatus().name())
            .build();
    }
}
