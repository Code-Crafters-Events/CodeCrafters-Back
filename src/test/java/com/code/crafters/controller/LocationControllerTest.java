package com.code.crafters.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.code.crafters.config.BaseIntegrationTest;
import com.code.crafters.dto.response.LocationResponseDTO;
import com.code.crafters.entity.Location;
import com.code.crafters.exception.GlobalExceptionHandler;
import com.code.crafters.mapper.LocationMapper;
import com.code.crafters.service.LocationService;

@SuppressWarnings("null")
@DisplayName("LocationController Tests")
class LocationControllerTest extends BaseIntegrationTest {

  private MockMvc mockMvc;
  private LocationService locationService;
  private LocationMapper locationMapper;

  @BeforeEach
  void setUp() {
    locationService = org.mockito.Mockito.mock(LocationService.class);
    locationMapper = org.mockito.Mockito.mock(LocationMapper.class);

    LocationController controller = new LocationController(locationService, locationMapper);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void shouldCreateLocationSuccessfully() throws Exception {
    Location location = new Location();
    location.setId(1L);

    LocationResponseDTO response = new LocationResponseDTO(
        1L, "IFEMA", "Av. del Partenon 5", "Madrid", "Madrid",
        "Espana", "28042", 40.4637, -3.6123);

    when(locationService.create(any())).thenReturn(location);
    when(locationMapper.toResponse(location)).thenReturn(response);

    String body = """
        {
          "venue":"IFEMA",
          "address":"Av. del Partenon 5",
          "city":"Madrid",
          "province":"Madrid",
          "country":"Espana",
          "zipCode":"28042",
          "latitude":40.4637,
          "longitude":-3.6123
        }
        """;

    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.venue").value("IFEMA"))
        .andExpect(jsonPath("$.city").value("Madrid"));
  }

  @Test
  void shouldReturnBadRequestForInvalidPayload() throws Exception {
    String body = """
        {
          "venue":"",
          "address":"Av. del Partenon 5",
          "city":"Madrid",
          "country":"Espana",
          "zipCode":"123"
        }
        """;

    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenLatitudeIsInvalid() throws Exception {
    String body = """
        {
          "venue":"IFEMA",
          "address":"Av. del Partenon 5",
          "city":"Madrid",
          "country":"Espana",
          "zipCode":"28042",
          "latitude":100.0,
          "longitude":-3.6123
        }
        """;

    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenLongitudeIsInvalid() throws Exception {
    String body = """
        {
          "venue":"IFEMA",
          "address":"Av. del Partenon 5",
          "city":"Madrid",
          "country":"Espana",
          "zipCode":"28042",
          "latitude":40.4637,
          "longitude":-200.0
        }
        """;

    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenCountryIsBlank() throws Exception {
    String body = """
        {
          "venue":"IFEMA",
          "address":"Av. del Partenon 5",
          "city":"Madrid",
          "country":"",
          "zipCode":"28042"
        }
        """;

    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenAddressIsBlank() throws Exception {
    String body = """
        {
          "venue":"IFEMA",
          "address":"",
          "city":"Madrid",
          "country":"Espana",
          "zipCode":"28042"
        }
        """;

    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenCityIsBlank() throws Exception {
    String body = """
        {
          "venue":"IFEMA",
          "address":"Av. del Partenon 5",
          "city":"",
          "country":"Espana",
          "zipCode":"28042"
        }
        """;

    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest());
  }

  

}
