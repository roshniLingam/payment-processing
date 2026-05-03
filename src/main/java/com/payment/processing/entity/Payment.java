package com.payment.processing.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;
import com.payment.processing.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    @Size(min = 16, max = 64)
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$")
    private String idempotencyKey;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    @Positive
    private BigDecimal amount;

    @Column(nullable = false)
    @NotBlank @Size(min = 3, max = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String providerReference;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @Column(nullable = false)
    private int retryCount = 0;

    @Column(nullable = false)
    private int maxRetries = 5;

    @PrePersist
    public void onCreate(){
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = Instant.now();
    }
}
