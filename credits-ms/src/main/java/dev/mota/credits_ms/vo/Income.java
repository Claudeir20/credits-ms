package dev.mota.credits_ms.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Embeddable
public record Income(
        @Column(name = "income", nullable = false, precision = 15, scale = 2)
        BigDecimal value
) {
    private static final BigDecimal MINIMUM_WAGE = new BigDecimal("1412.00");

    public Income {
        if (value == null) {
            throw new IllegalArgumentException("Income is required");
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Income must be greater than zero");
        }

        if (value.compareTo(MINIMUM_WAGE) < 0) {
            throw new IllegalArgumentException("Income must be at least R$ 1.412,00");
        }

        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isAtLeast(BigDecimal minimum) {
        return this.value.compareTo(minimum) >= 0;
    }

    @Override
    public String toString() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(value);
    }
}