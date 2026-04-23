package com.code.crafters.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentIntentResponseDTO Tests")
class PaymentIntentResponseDTOTest {

    @Test
    void shouldCreateRecordCorrectly() {
        PaymentIntentResponseDTO dto = new PaymentIntentResponseDTO(
                "secret_123",
                "pi_123",
                BigDecimal.valueOf(49.99),
                "eur",
                5L,
                "http://localhost:8080/uploads/qr/test.png",
                "verification-code");

        assertEquals("secret_123", dto.clientSecret());
        assertEquals("pi_123", dto.paymentIntentId());
        assertEquals(BigDecimal.valueOf(49.99), dto.amount());
        assertEquals("eur", dto.currency());
        assertEquals(5L, dto.ticketId());
    }
}
