package com.payment.processing.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.payment.processing.enums.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(columnNames = "idempotencyKey")
})
public class Payment {
    @Id
    @GeneratedValue
    private UUID id;

    private String idempotencyKey;
    private String userId;
    private Long amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String providerReference;

    private Instant createdAt;
    private Instant updatedAt;

}
