package dev.mota.credits_ms.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mota.credits_ms.Event.produced.CreditRequestedEvent;
import dev.mota.credits_ms.config.RabbitMQConfig;
import dev.mota.credits_ms.model.CreditRequest;
import dev.mota.credits_ms.model.OutboxEvent;
import dev.mota.credits_ms.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(CreditRequest entity) {
        var event = CreditRequestedEvent.from(entity);

        var outbox = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(entity.getId())
                .eventType(RabbitMQConfig.CREDIT_REQUESTED_ROUTING_KEY)
                .payload(serialize(event))
                .correlationId(entity.getCorrelationId())
                .published(false)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(outbox);
        log.debug("Outbox event saved correlationId={} aggregateId={}",
                entity.getCorrelationId(), entity.getId());
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        var pending = outboxRepository.findByPublishedFalse();

        if (pending.isEmpty()) return;

        log.info("Publishing {} pending outbox events", pending.size());

        pending.forEach(event -> {
            try {
                rabbitTemplate.convertAndSend(
                        "credit.exchange",
                        event.getEventType(),
                        event.getPayload()
                );
                event.markAsPublished();
                outboxRepository.save(event);
                log.info("Event published correlationId={} eventType={}",
                        event.getCorrelationId(), event.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish event correlationId={} error={}",
                        event.getCorrelationId(), e.getMessage());
            }
        });
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }
    }
}