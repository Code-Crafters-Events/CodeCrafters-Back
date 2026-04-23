package com.code.crafters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.code.crafters.dto.request.LocationRequestDTO;
import com.code.crafters.dto.response.LocationResponseDTO;
import com.code.crafters.entity.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("LocationMapper Tests")
class LocationMapperTest {

    private final LocationMapper mapper = Mappers.getMapper(LocationMapper.class);

    @Test
    void shouldMapRequestToEntity() {
        LocationRequestDTO dto = new LocationRequestDTO(
                "IFEMA",
                "Av. del Partenon 5",
                "Madrid",
                "Madrid",
                "Espana",
                "28042",
                40.4637,
                -3.6123);

        Location result = mapper.toEntity(dto);

        assertEquals("IFEMA", result.getVenue());
        assertEquals("Madrid", result.getCity());
        assertEquals("28042", result.getZipCode());
    }

    @Test
    void shouldMapEntityToResponse() {
        Location location = new Location();
        location.setId(1L);
        location.setVenue("IFEMA");
        location.setAddress("Av. del Partenon 5");
        location.setCity("Madrid");
        location.setProvince("Madrid");
        location.setCountry("Espana");
        location.setZipCode("28042");
        location.setLatitude(40.4637);
        location.setLongitude(-3.6123);

        LocationResponseDTO response = mapper.toResponse(location);

        assertEquals(1L, response.id());
        assertEquals("IFEMA", response.venue());
        assertEquals("Madrid", response.city());
    }
}
