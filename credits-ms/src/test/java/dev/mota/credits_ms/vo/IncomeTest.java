package dev.mota.credits_ms.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IncomeTest {


    @Test
    void shouldCreateIncomeWhenValid(){

        Income income = new Income(new BigDecimal("5000.00"));

        assertEquals( new BigDecimal("5000.00"), income.value());
    }


    @Test
    void shouldAroundIncomeToTwoDecimalPlaces(){

        Income income = new Income(new BigDecimal("5000.555"));

        assertEquals(new BigDecimal("5000.56"), income.value());

    }

    @Test
    void shouldRejectWhenIncomeNull(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Income(null);
        });

    }


    @Test
    void shouldRejectIncomeBelowMinimumWage(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Income(new BigDecimal("1000.00"));
        });

    }
}