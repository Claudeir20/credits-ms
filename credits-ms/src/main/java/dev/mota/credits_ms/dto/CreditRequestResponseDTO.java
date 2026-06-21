package dev.mota.credits_ms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditRequestResponseDTO(
        UUID id,
        String name,
        String cpf,
        BigDecimal income,
        BigDecimal valueRequest,
        int termMonths,
        String status,
        UUID correlationId,
        LocalDateTime createdAt
) {}