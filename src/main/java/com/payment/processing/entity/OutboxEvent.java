package com.payment.processing.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;

import com.payment.processing.enums.EventType;
import com.payment.processing.enums.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "outbox_event_index", columnList = "status")
})
public class OutboxEvent {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID eventId;
    @Column(nullable = false)

    private String aggregateType;
    @Column(nullable = false)
    private String aggregateId;
    @Column(nullable = false)
    private EventType eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = true)
    private Instant publishedAt;

}
