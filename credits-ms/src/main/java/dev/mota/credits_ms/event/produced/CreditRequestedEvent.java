package dev.mota.credits_ms.event.produced;

import dev.mota.credits_ms.model.CreditRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public record CreditRequestedEvent(
        UUID eventId,
        UUID requestId,
        String cpf,
        String name,
        String email,
        BigDecimal income,
        BigDecimal valueRequest,
        int termMonths,
        UUID correlationId,
        LocalDateTime occurredAt
) {
    public static CreditRequestedEvent from(CreditRequest entity) {
        return new CreditRequestedEvent(
                UUID.randomUUID(),
                entity.getId(),
                entity.getCpf().value(),
                entity.getName(),
                entity.getEmail(),
                entity.getIncome().value(),
                entity.getValueRequest(),
                entity.getTermMonths(),
                entity.getCorrelationId(),
                LocalDateTime.now()
        );
    }
}
