package com.code.crafters.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.code.crafters.entity.User;
import com.code.crafters.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("null")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth and User Controller Integration Tests")
@Transactional
class AuthAndUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = new User();
        testUser.setName("Juan");
        testUser.setFirstName("Pérez");
        testUser.setEmail("juan@example.com");
        testUser.setAlias("juanperez");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);
    }

    private String createRegisterJson(String name, String firstName, String alias,
            String email, String password) throws Exception {
        return objectMapper.writeValueAsString(
                new UserRegisterRequest(name, firstName, "", alias, email, password, null));
    }

    private String createLoginJson(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(
                new UserLoginRequest(email, password));
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() throws Exception {
        String requestBody = createRegisterJson("Maria", "García", "mariag", "maria@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", equalTo("maria@example.com")))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        String requestBody = createRegisterJson("Other", "User", "otheruser", "juan@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        String requestBody = createLoginJson("juan@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void testLoginWrongPassword() throws Exception {
        String requestBody = createLoginJson("juan@example.com", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Should get all users")
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser
    @DisplayName("Should get user by id")
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(testUser.getId().intValue())));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 for non-existing user")
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Should update user successfully")
    void testUpdateUserSuccess() throws Exception {
        String updateJson = createRegisterJson("Juan Carlos", "Pérez", "juanperez", "juan@example.com", "password123");
        mockMvc.perform(put("/api/v1/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Juan Carlos")));
    }

    @Test
    @WithMockUser
    @DisplayName("Should reject update for non-existing user")
    void testUpdateUserNotFound() throws Exception {
        String updateJson = createRegisterJson("Test", "User", "test", "test@example.com", "password123");
        mockMvc.perform(put("/api/v1/users/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Should delete user successfully")
    void testDeleteUserSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should validate email format on registration")
    void testRegisterInvalidEmail() throws Exception {
        String requestBody = createRegisterJson("Test", "User", "testuser", "invalidemail", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    static class UserRegisterRequest {
        public String name;
        public String firstName;
        public String secondName;
        public String alias;
        public String email;
        public String password;
        public String profileImage;

        public UserRegisterRequest(String name, String firstName, String secondName,
                String alias, String email, String password, String profileImage) {
            this.name = name;
            this.firstName = firstName;
            this.secondName = secondName;
            this.alias = alias;
            this.email = email;
            this.password = password;
            this.profileImage = profileImage;
        }
    }

    static class UserLoginRequest {
        public String email;
        public String password;

        public UserLoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}