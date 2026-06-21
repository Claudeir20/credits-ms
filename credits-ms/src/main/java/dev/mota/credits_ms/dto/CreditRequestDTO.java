package dev.mota.credits_ms.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreditRequestDTO(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "CPF is required")
        @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF format is invalid")
        String cpf,

        @NotNull(message = "Income is required")
        @DecimalMin(value = "1412.00", message = "Income must be at least R$ 1.412,00")
        @Digits(integer = 13, fraction = 2)
        BigDecimal income,

        @NotNull(message = "Value request is required")
        @DecimalMin(value = "0.01", message = "Value request must be greater than zero")
        @DecimalMax(value = "5000000.00", message = "Value request cannot exceed R$ 5.000.000,00")
        @Digits(integer = 13, fraction = 2)
        BigDecimal valueRequest,

        @NotNull(message = "Term months is required")
        @Min(value = 6, message = "Minimum term is 6 months")
        @Max(value = 360, message = "Maximum term is 360 months")
        Integer termMonths
) {}