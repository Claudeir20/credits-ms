package dev.mota.credits_ms.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfTest {

    @Test
    void shouldCreateCpfWhenValid(){
        Cpf cpf = new Cpf("935.411.347.80");

        assertEquals("93541134780", cpf.value());
    }

    @Test
    void shouldRejectCpfWhenNull(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf(null);
        });
    }

    @Test
    void shouldRejectCpfWhenAllDigitsAreEquals(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf("222.222.222.22");
        });
    }


    @Test
    void shouldRejectCpfWhenInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf("935.411.347-81");
        });
    }

}