package com.code.crafters.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentIntentRequestDTO Tests")
class PaymentIntentRequestDTOTest {

    @Test
    void shouldCreateRecordCorrectly() {
        PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(10L, 20L);

        assertEquals(10L, dto.userId());
        assertEquals(20L, dto.eventId());
    }
}
