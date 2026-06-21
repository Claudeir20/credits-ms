package dev.mota.credits_ms.Event.produced;

import dev.mota.credits_ms.model.CreditRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public record CreditRequestedEvent(
        UUID requestId,
        String cpf,
        BigDecimal income,
        BigDecimal valueRequest,
        int termMonths,
        UUID correlationId
) {
    public static CreditRequestedEvent from(CreditRequest entity) {
        return new CreditRequestedEvent(
                entity.getId(),
                entity.getCpf().value(),
                entity.getIncome().value(),
                entity.getValueRequest(),
                entity.getTermMonths(),
                entity.getCorrelationId()
        );
    }
}
