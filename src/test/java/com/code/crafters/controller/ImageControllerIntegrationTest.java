package com.code.crafters.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.code.crafters.config.BaseIntegrationTest;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
                "app.base-url=http://localhost:8080"
})
@SuppressWarnings("null")
@DisplayName("ImageController Integration Tests")
class ImageControllerIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private User user;
        private Event event;

        @BeforeEach
        void setUp() {
                eventRepository.deleteAll();
                userRepository.deleteAll();

                User testUser = new User();
                testUser.setName("Juan");
                testUser.setFirstName("Perez");
                testUser.setEmail("juan@example.com");
                testUser.setAlias("juanp");
                testUser.setPassword(passwordEncoder.encode("password123"));
                this.user = userRepository.save(testUser);

                Event testEvent = new Event();
                testEvent.setTitle("Workshop Java");
                testEvent.setDescription("Descripcion valida del evento");
                testEvent.setType(EventType.TALLER);
                testEvent.setDate(LocalDate.now().plusDays(10));
                testEvent.setTime(LocalTime.of(18, 0));
                testEvent.setMaxAttendees(20);
                testEvent.setCategory(EventCategory.PRESENCIAL);
                testEvent.setPrice(BigDecimal.TEN);
                testEvent.setAuthor(user);
                this.event = eventRepository.save(testEvent);
        }

        @Test
        void shouldUploadEventImageSuccessfully() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "file", "event.png", "image/png", "image-content".getBytes());

                mockMvc.perform(multipart("/api/v1/images/events/{eventId}", event.getId())
                                .file(file)
                                .param("userId", user.getId().toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.imageUrl").value(containsString("/uploads/events/")));
        }

        @Test
        void shouldUploadProfileImageSuccessfully() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "file", "avatar.png", "image/png", "avatar-content".getBytes());

                mockMvc.perform(multipart("/api/v1/images/users/{userId}", user.getId())
                                .file(file))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.imageUrl").value(containsString("/uploads/avatars/")));
        }
}
