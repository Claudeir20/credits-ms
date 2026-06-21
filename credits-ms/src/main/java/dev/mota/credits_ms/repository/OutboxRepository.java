package dev.mota.credits_ms.repository;

import dev.mota.credits_ms.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByPublishedFalse();

    List<OutboxEvent> findByAggregateId(UUID aggregateId);

    boolean existsByCorrelationIdAndPublishedTrue(UUID correlationId);
}