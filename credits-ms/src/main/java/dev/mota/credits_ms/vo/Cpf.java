package dev.mota.credits_ms.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Cpf(
        @Column(name = "cpf", nullable = false, length = 11)
        String value
) {
    public Cpf {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CPF is required");
        }

        value = value.replaceAll("[.\\-]", "");

        if (!value.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF must contain 11 digits");
        }

        if (!isValid(value)) {
            throw new IllegalArgumentException("CPF is invalid");
        }
    }

    private static boolean isValid(String cpf) {
        if (cpf.chars().distinct().count() == 1) return false;

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = (sum * 10) % 11;
        if (firstDigit == 10) firstDigit = 0;
        if (firstDigit != (cpf.charAt(9) - '0')) return false;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = (sum * 10) % 11;
        if (secondDigit == 10) secondDigit = 0;

        return secondDigit == (cpf.charAt(10) - '0');
    }

    @Override
    public String toString() {
        return value.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
