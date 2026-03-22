package com.payment.processing.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payment.processing.entity.OutboxEvent;
import com.payment.processing.enums.OutboxStatus;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID>{
    List<OutboxEvent> findTop100ByStatus(OutboxStatus status);
}
