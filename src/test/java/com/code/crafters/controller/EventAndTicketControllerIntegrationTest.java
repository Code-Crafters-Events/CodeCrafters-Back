package com.code.crafters.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

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

import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;
import com.code.crafters.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("null")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Event and Ticket Controller Integration Tests")
@Transactional
class EventAndTicketControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private TicketRepository ticketRepository;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private User testUser;
        private Event testEvent;
        private String authToken;

        @BeforeEach
        void setUp() {
                ticketRepository.deleteAll();
                eventRepository.deleteAll();
                userRepository.deleteAll();

                testUser = new User();
                testUser.setName("Juan");
                testUser.setFirstName("Pérez");
                testUser.setEmail("juan@example.com");
                testUser.setAlias("juanperez");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser = userRepository.save(testUser);

                authToken = "Bearer " + jwtService.generateToken(testUser.getId(), testUser.getEmail());

                testEvent = new Event();
                testEvent.setTitle("Masterclass Java");
                testEvent.setDescription("Una masterclass completa sobre Java");
                testEvent.setType(EventType.MASTERCLASS);
                testEvent.setDate(LocalDate.now().plusDays(10));
                testEvent.setTime(LocalTime.of(14, 0));
                testEvent.setMaxAttendees(50);
                testEvent.setCategory(EventCategory.PRESENCIAL);
                testEvent.setPrice(BigDecimal.valueOf(29.99));
                testEvent.setAuthor(testUser);
                testEvent = eventRepository.save(testEvent);
        }

        private String createEventJson(String title, String description, String type,
                        LocalDate date, LocalTime time, Integer maxAttendees,
                        String category, String price) throws Exception {
                return objectMapper.writeValueAsString(
                                new EventRequest(title, description, type, date, time, maxAttendees,
                                                null, category, price, null));
        }

        @Test
        @DisplayName("Should create event successfully")
        void testCreateEventSuccess() throws Exception {
                String requestBody = createEventJson(
                                "New Workshop", "A great workshop", "TALLER",
                                LocalDate.now().plusDays(5), LocalTime.of(15, 0), 30,
                                "ONLINE", "19.99");

                mockMvc.perform(post("/api/v1/events")
                                .header("Authorization", authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title", equalTo("New Workshop")));
        }

        @Test
        @DisplayName("Should get all events paginated")
        void testGetAllEventsSuccess() throws Exception {
                mockMvc.perform(get("/api/v1/events")
                                .param("page", "0")
                                .param("size", "15"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Should return 404 for non-existing event")
        void testGetEventByIdNotFound() throws Exception {
                mockMvc.perform(get("/api/v1/events/{id}", 999L))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should update event successfully")
        void testUpdateEventSuccess() throws Exception {
                String requestBody = createEventJson(
                                "Updated Title", "New description", "MASTERCLASS",
                                LocalDate.now().plusDays(10), LocalTime.of(16, 0), 60,
                                "PRESENCIAL", "39.99");

                mockMvc.perform(put("/api/v1/events/{id}", testEvent.getId())
                                .header("Authorization", authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title", equalTo("Updated Title")));
        }

        @Test
        @DisplayName("Should reject event update by non-owner")
        void testUpdateEventNotOwner() throws Exception {
                User otherUser = new User();
                otherUser.setName("Carlos");
                otherUser.setFirstName("López");
                otherUser.setSecondName("García");
                otherUser.setAlias("carloslop");
                otherUser.setEmail("carlos@example.com");
                otherUser.setPassword(passwordEncoder.encode("password123"));
                otherUser = userRepository.save(otherUser);

                String otherUserToken = "Bearer " + jwtService.generateToken(otherUser.getId(), otherUser.getEmail());
                String requestBody = objectMapper.writeValueAsString(
                                new EventRequest(
                                                "Hack Attempt",
                                                testEvent.getDescription(),
                                                testEvent.getType().name(),
                                                testEvent.getDate(),
                                                testEvent.getTime(),
                                                testEvent.getMaxAttendees(),
                                                null,
                                                testEvent.getCategory().name(),
                                                testEvent.getPrice().toString(),
                                                null));

                mockMvc.perform(put("/api/v1/events/{id}", testEvent.getId())
                                .header("Authorization", otherUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andDo(print())
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should search events with filters")
        void testSearchEventsSuccess() throws Exception {
                mockMvc.perform(get("/api/v1/events/search")
                                .param("title", "Masterclass"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Should validate future date")
        void testCreateEventPastDate() throws Exception {
                String requestBody = createEventJson("Past", "Desc", "TALLER", LocalDate.now().minusDays(1),
                                LocalTime.now(),
                                10, "ONLINE", "0.0");

                mockMvc.perform(post("/api/v1/events")
                                .header("Authorization", authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should register user to event")
        void testRegisterToEventSuccess() throws Exception {
                mockMvc.perform(post("/api/v1/tickets")
                                .header("Authorization", authToken)
                                .param("eventId", testEvent.getId().toString()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.userId", equalTo(testUser.getId().intValue())));
        }

        @Test
        @DisplayName("Should reject duplicate registration")
        void testRegisterToEventDuplicate() throws Exception {
                Ticket ticket = new Ticket();
                ticket.setUser(testUser);
                ticket.setEvent(testEvent);
                ticket.setPaymentStatus(PaymentStatus.PENDING);
                ticketRepository.save(ticket);

                mockMvc.perform(post("/api/v1/tickets")
                                .header("Authorization", authToken)
                                .param("eventId", testEvent.getId().toString()))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should get tickets by user")
        void testGetTicketsByUserSuccess() throws Exception {
                Ticket ticket = new Ticket();
                ticket.setUser(testUser);
                ticket.setEvent(testEvent);
                ticket.setPaymentStatus(PaymentStatus.PENDING);
                ticketRepository.save(ticket);

                mockMvc.perform(get("/api/v1/tickets/user/{userId}", testUser.getId())
                                .header("Authorization", authToken)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Should delete event successfully")
        void testDeleteEventSuccess() throws Exception {
                mockMvc.perform(delete("/api/v1/events/{id}", testEvent.getId())
                                .header("Authorization", authToken))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should get event by id successfully")
        void testGetEventByIdSuccess() throws Exception {
                mockMvc.perform(get("/api/v1/events/{id}", testEvent.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", equalTo(testEvent.getId().intValue())))
                                .andExpect(jsonPath("$.title", equalTo(testEvent.getTitle())));
        }

        static class EventRequest {
                public String title;
                public String description;
                public String type;
                public LocalDate date;
                public LocalTime time;
                public Integer maxAttendees;
                public Long locationId;
                public String category;
                public String price;
                public String imageUrl;

                public EventRequest(String title, String description, String type,
                                LocalDate date, LocalTime time, Integer maxAttendees,
                                Long locationId, String category, String price,
                                String imageUrl) {
                        this.title = title;
                        this.description = description;
                        this.type = type;
                        this.date = date;
                        this.time = time;
                        this.maxAttendees = maxAttendees;
                        this.locationId = locationId;
                        this.category = category;
                        this.price = price;
                        this.imageUrl = imageUrl;
                }
        }
}