package com.code.crafters.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocationRequestDTO Tests")
class LocationRequestDTOTest {

    @Test
    void shouldCreateRecordCorrectly() {
        LocationRequestDTO dto = new LocationRequestDTO(
                "IFEMA",
                "Av. del Partenon 5",
                "Madrid",
                "Madrid",
                "Espana",
                "28042",
                40.4637,
                -3.6123);

        assertEquals("IFEMA", dto.venue());
        assertEquals("Madrid", dto.city());
        assertEquals("28042", dto.zipCode());
        assertEquals(40.4637, dto.latitude());
        assertEquals(-3.6123, dto.longitude());
    }
}
