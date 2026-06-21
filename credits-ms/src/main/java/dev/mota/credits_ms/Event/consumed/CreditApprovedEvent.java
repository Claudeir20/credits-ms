package dev.mota.credits_ms.Event.consumed;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditApprovedEvent(
        UUID eventId,
        UUID requestId,
        LocalDateTime occurred
) {
}
