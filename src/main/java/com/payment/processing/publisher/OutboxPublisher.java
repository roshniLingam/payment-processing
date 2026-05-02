package com.payment.processing.publisher;

import java.time.Instant;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.payment.processing.entity.OutboxEvent;
import com.payment.processing.enums.OutboxStatus;
import com.payment.processing.repository.OutboxEventRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    public void publishEvents(){
        List<OutboxEvent> events = outboxEventRepository.findTop100ByStatus(OutboxStatus.NEW);

        for(OutboxEvent event: events){
            kafkaTemplate.send("payment-events", event.getAggregateId().toString(), event.getPayload());

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            outboxEventRepository.save(event);
        }
    }

}
