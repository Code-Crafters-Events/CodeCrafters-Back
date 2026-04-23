package com.code.crafters.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocationResponseDTO Tests")
class LocationResponseDTOTest {

    @Test
    void shouldCreateRecordCorrectly() {
        LocationResponseDTO dto = new LocationResponseDTO(
                1L,
                "IFEMA",
                "Av. del Partenon 5",
                "Madrid",
                "Madrid",
                "Espana",
                "28042",
                40.4637,
                -3.6123
        );

        assertEquals(1L, dto.id());
        assertEquals("IFEMA", dto.venue());
        assertEquals("Madrid", dto.city());
        assertEquals("28042", dto.zipCode());
    }
}
