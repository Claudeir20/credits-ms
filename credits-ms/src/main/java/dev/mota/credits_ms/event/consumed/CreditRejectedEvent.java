package dev.mota.credits_ms.event.consumed;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditRejectedEvent(
        UUID eventId,
        UUID requestId,
        String name,
        String email,
        UUID correlationId,
        LocalDateTime occurred
) {
}
