package dev.mota.credits_ms.model;

import dev.mota.credits_ms.enums.Status;
import dev.mota.credits_ms.vo.Cpf;
import dev.mota.credits_ms.vo.Income;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Entity
@Table(name = "credit_requests")
public class CreditRequest {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cpf", nullable = false, length = 11))
    private Cpf cpf;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "income", nullable = false, precision = 15, scale = 2))
    private Income income;

    @Column(name = "value_request", nullable = false, precision = 15, scale = 2)
    private BigDecimal valueRequest;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "correlation_id", nullable = false, updatable = false)
    private UUID correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CreditRequest() {}

    private CreditRequest(UUID id, Cpf cpf, String name, Income income,
                          BigDecimal valueRequest, int termMonths, Status status,
                          UUID correlationId, LocalDateTime createdAt) {
        this.id = id;
        this.cpf = cpf;
        this.name = name;
        this.income = income;
        this.valueRequest = valueRequest;
        this.termMonths = termMonths;
        this.status = status;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
    }
    public static CreditRequest request(Cpf cpf, String name, Income income, BigDecimal valueRequest, int termMonths) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (valueRequest == null) {
            throw new IllegalArgumentException("Value request is required");
        }

        if (valueRequest.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Value request must be greater than zero");
        }

        if (valueRequest.compareTo(income.value().multiply(BigDecimal.TEN)) > 0) {
            throw new IllegalArgumentException("Value request cannot exceed 10x the income");
        }

        if ( termMonths < 6 || termMonths > 360) {
            throw new IllegalArgumentException("Term must be between 6 and 360 months");
        }

        return new CreditRequest(
                UUID.randomUUID(),
                cpf,
                name,
                income,
                valueRequest,
                termMonths,
                Status.PENDING,
                UUID.randomUUID(),
                LocalDateTime.now()
        );
    }

    public void approve() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }
        this.status = Status.APPROVED;
    }

    public void reject() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }
        this.status = Status.REJECTED;
    }
}