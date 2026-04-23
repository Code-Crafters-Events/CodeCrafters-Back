package com.code.crafters.e2e;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.code.crafters.entity.Event;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("End-to-End Tests - Complete User Workflow")
class EndToEndWorkflowTest {

        @LocalServerPort
        private int port;

        @Autowired
        private UserRepository userRepository;
        @Autowired
        private EventRepository eventRepository;
        @Autowired
        private TicketRepository ticketRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        private String userToken;
        private Long userId;

        @BeforeEach
        void setUp() {
                RestAssured.port = port;
                ticketRepository.deleteAll();
                eventRepository.deleteAll();
                userRepository.deleteAll();
        }

        @Test
        @DisplayName("E2E: Complete workflow from registration to ticket verification")
        void testCompleteUserWorkflow() {
                String registerResponse = given()
                                .contentType(ContentType.JSON)
                                .body(Map.of(
                                                "name", "Maria",
                                                "firstName", "García",
                                                "alias", "mariag",
                                                "email", "maria@example.com",
                                                "password", "password123"))
                                .post("/api/auth/register")
                                .then()
                                .statusCode(201)
                                .extract().asString();

                userId = JsonPath.from(registerResponse).getLong("id");
                userToken = "Bearer " + JsonPath.from(registerResponse).getString("token");
                String createEventResponse = given()
                                .header("Authorization", userToken)
                                .contentType(ContentType.JSON)
                                .body(Map.of(
                                                "title", "Advanced Java Workshop",
                                                "description", "Detailed description for database constraints",
                                                "type", "TALLER",
                                                "date", LocalDate.now().plusDays(15).toString(),
                                                "time", "14:00",
                                                "maxAttendees", 25,
                                                "category", "PRESENCIAL",
                                                "price", "49.99"))
                                .post("/api/v1/events")
                                .then()
                                .statusCode(201)
                                .extract().asString();

                Long eventIdFromApi = JsonPath.from(createEventResponse).getLong("id");
                assertThat(eventIdFromApi, is(notNullValue()));
                User carlos = new User();
                carlos.setName("Carlos");
                carlos.setFirstName("López");
                carlos.setAlias("carloslop");
                carlos.setEmail("carlos@example.com");
                carlos.setPassword(passwordEncoder.encode("password123"));
                carlos = userRepository.save(carlos);

                Event testEvent = new Event();
                testEvent.setTitle("Networking Event");
                testEvent.setDescription("Mandatory description for manual event");
                testEvent.setType(EventType.NETWORKING);
                testEvent.setDate(LocalDate.now().plusDays(20));
                testEvent.setTime(LocalTime.of(18, 0));
                testEvent.setMaxAttendees(100);
                testEvent.setCategory(EventCategory.PRESENCIAL);
                testEvent.setPrice(BigDecimal.ZERO);
                testEvent.setAuthor(carlos);
                testEvent = eventRepository.save(testEvent);

                given()
                                .header("Authorization", userToken)
                                .param("eventId", testEvent.getId())
                                .when()
                                .post("/api/v1/tickets")
                                .then()
                                .statusCode(201);
                given()
                                .header("Authorization", userToken)
                                .when()
                                .get("/api/v1/tickets/user/{id}", userId)
                                .then()
                                .statusCode(200)
                                .body("content", hasSize(greaterThan(0)));
        }

        @Test
        @DisplayName("E2E: Event creator operations")
        void testEventCreatorOperations() {
                String regResponse = given()
                                .contentType(ContentType.JSON)
                                .body(Map.of(
                                                "name", "Pedro", "firstName", "Martínez", "alias", "pedrom",
                                                "email", "pedro@example.com", "password", "password123"))
                                .post("/api/auth/register")
                                .then().statusCode(201).extract().asString();

                String token = "Bearer " + JsonPath.from(regResponse).getString("token");
                Long pedroId = JsonPath.from(regResponse).getLong("id");

                Long eventId = given()
                                .header("Authorization", token)
                                .contentType(ContentType.JSON)
                                .body(Map.of(
                                                "title", "Hackathon", "description", "24h coding", "type", "HACKATHON",
                                                "date", LocalDate.now().plusDays(30).toString(), "time", "10:00",
                                                "maxAttendees", 50, "category", "PRESENCIAL", "price", "0.00"))
                                .post("/api/v1/events")
                                .then().statusCode(201).extract().jsonPath().getLong("id");

                given()
                                .when()
                                .get("/api/v1/events/user/{id}", pedroId)
                                .then().statusCode(200).body("content", hasSize(1));

                given()
                                .header("Authorization", token)
                                .delete("/api/v1/events/{id}", eventId)
                                .then().statusCode(204);
        }
}