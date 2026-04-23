package com.code.crafters.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.code.crafters.entity.User;
import com.code.crafters.repository.UserRepository;
import com.code.crafters.security.JwtService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
@DisplayName("LocationController Integration Tests")
class LocationControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtService jwtService;

  private String authToken;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    User testUser = new User();
    testUser.setName("Juan");
    testUser.setFirstName("Perez");
    testUser.setEmail("juan@example.com");
    testUser.setAlias("juanp");
    testUser.setPassword(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    authToken = "Bearer " + jwtService.generateToken(testUser.getId(), testUser.getEmail());
  }

  @Test
  void shouldCreateLocationSuccessfully() throws Exception {
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
        .header("Authorization", authToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.venue").value(equalTo("IFEMA")))
        .andExpect(jsonPath("$.city").value(equalTo("Madrid")));
  }

  @Test
  void shouldReturnForbiddenWhenNoTokenProvided() throws Exception {
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
        .andExpect(status().isForbidden());
  }
}
