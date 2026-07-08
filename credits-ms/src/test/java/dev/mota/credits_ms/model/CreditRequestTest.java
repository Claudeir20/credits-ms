package dev.mota.credits_ms.model;

import dev.mota.credits_ms.enums.Status;
import dev.mota.credits_ms.vo.Cpf;
import dev.mota.credits_ms.vo.Income;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreditRequestTest {


    @Test
    void shouldCreateRequestWithPendingStatus(){
        CreditRequest request = CreditRequest.request(
                new Cpf("935.411.347-80"),
                "Jose Mota",
                "jose@email.com",
                new Income(new BigDecimal("5000.00")),
                new BigDecimal("20000.00"),
                24
        );

        assertNotNull(request.getId());
        assertNotNull(request.getCorrelationId());
        assertNotNull(request.getCreatedAt());
        assertEquals(Status.PENDING, request.getStatus());
    }


    @Test
    void shouldRejectWhenNameIsBlank(){

        assertThrows(IllegalArgumentException.class, () -> {

            CreditRequest.request(
                    new Cpf("935.411.347-80"),
                    "",
                    "jose@email.com",
                    new Income(new BigDecimal("5000.00")),
                    new BigDecimal("20000.00"),
                    24
            );
        });
    }

    @Test
    void shouldRejectWhenValueRequestExceedsTenTimesIncome(){

        assertThrows(IllegalArgumentException.class, () -> {
            CreditRequest.request(
                    new Cpf("935.411.347-80"),
                    "Jose Mota",
                    "jose@email.com",
                    new Income(new BigDecimal("5000.00")),
                    new BigDecimal("60000.00"),
                    24
            );
        });
    }

    @Test
    void shouldApprovePendingRequest(){
        CreditRequest request = CreditRequest.request(
                new Cpf("935.411.347-80"),
                "Jose Mota",
                "jose@email.com",
                new Income(new BigDecimal("5000.00")),
                new BigDecimal("50000.00"),
                24
        );

        request.approve();

        assertEquals(Status.APPROVED, request.getStatus());

    }

    @Test
    void shouldNotRejectApprovedRequest() {
        CreditRequest request = CreditRequest.request(
                new Cpf("935.411.347-80"),
                "Jose Mota",
                "jose@email.com",
                new Income(new BigDecimal("5000.00")),
                new BigDecimal("20000.00"),
                24
        );

        request.approve();

        assertThrows(IllegalStateException.class, request::reject);
    }
}
