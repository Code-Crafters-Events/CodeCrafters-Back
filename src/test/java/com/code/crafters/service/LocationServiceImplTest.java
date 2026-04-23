package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.code.crafters.dto.request.LocationRequestDTO;
import com.code.crafters.entity.Location;
import com.code.crafters.mapper.LocationMapper;
import com.code.crafters.repository.LocationRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("LocationServiceImpl Unit Tests")
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    private LocationRequestDTO requestDTO;
    private Location location;

    @BeforeEach
    void setUp() {
        requestDTO = new LocationRequestDTO(
                "IFEMA",
                "Av. del Partenon 5",
                "Madrid",
                "Madrid",
                "Espana",
                "28042",
                40.4637,
                -3.6123);

        location = new Location();
        location.setId(1L);
        location.setVenue("IFEMA");
        location.setAddress("Av. del Partenon 5");
        location.setCity("Madrid");
        location.setProvince("Madrid");
        location.setCountry("Espana");
        location.setZipCode("28042");
        location.setLatitude(40.4637);
        location.setLongitude(-3.6123);
    }

    @Test
    @DisplayName("Should create location successfully")
    void createShouldSaveMappedLocation() {
        when(locationMapper.toEntity(requestDTO)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(location);

        Location result = locationService.create(requestDTO);

        assertEquals(1L, result.getId());
        assertEquals("IFEMA", result.getVenue());
        verify(locationMapper).toEntity(requestDTO);
        verify(locationRepository).save(location);
    }
}
