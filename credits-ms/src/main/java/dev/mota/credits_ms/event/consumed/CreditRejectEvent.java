package dev.mota.credits_ms.event.consumed;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditRejectEvent(
        UUID eventId,
        UUID requestId,
        LocalDateTime occurred
) {
}
