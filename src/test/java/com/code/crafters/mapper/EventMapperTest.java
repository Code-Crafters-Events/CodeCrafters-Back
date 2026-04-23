package com.code.crafters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.entity.enums.PaymentStatus;

@SpringBootTest(classes = {
        EventMapperImpl.class,
        LocationMapperImpl.class
})
@DisplayName("EventMapper Tests")
class EventMapperTest {

    @Autowired
    private EventMapper mapper;

    @Test
    void shouldMapRequestToEntity() {
        EventRequestDTO dto = new EventRequestDTO(
                "Masterclass Java",
                "Descripcion amplia",
                EventType.MASTERCLASS,
                LocalDate.now().plusDays(10),
                LocalTime.of(18, 0),
                30,
                null,
                EventCategory.PRESENCIAL,
                BigDecimal.valueOf(29.99),
                "http://localhost/image.png");

        Event result = mapper.toEntity(dto);

        assertEquals("Masterclass Java", result.getTitle());
        assertEquals(EventType.MASTERCLASS, result.getType());
        assertEquals(EventCategory.PRESENCIAL, result.getCategory());
        assertEquals(BigDecimal.valueOf(29.99), result.getPrice());
    }

    @Test
    void shouldMapEntityToResponseAndCountOnlyValidAttendees() {
        User author = new User();
        author.setId(1L);
        author.setName("Juan");
        author.setAlias("juanp");

        Ticket completed = new Ticket();
        completed.setPaymentStatus(PaymentStatus.COMPLETED);

        Ticket free = new Ticket();
        free.setPaymentStatus(PaymentStatus.FREE);

        Ticket pending = new Ticket();
        pending.setPaymentStatus(PaymentStatus.PENDING);

        Ticket failed = new Ticket();
        failed.setPaymentStatus(PaymentStatus.FAILED);

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Evento");
        event.setDescription("Desc");
        event.setType(EventType.TALLER);
        event.setDate(LocalDate.now().plusDays(5));
        event.setTime(LocalTime.of(17, 0));
        event.setMaxAttendees(100);
        event.setCategory(EventCategory.ONLINE);
        event.setPrice(BigDecimal.TEN);
        event.setImageUrl("img");
        event.setAuthor(author);
        event.setTickets(List.of(completed, free, pending, failed));
        event.setLocation(null);

        EventResponseDTO response = mapper.toResponse(event);

        assertEquals(1L, response.authorId());
        assertEquals("Juan", response.authorName());
        assertEquals("juanp", response.authorAlias());
        assertEquals(2, response.attendeesCount());
    }

    @Test
    void shouldMapEntityToResponseWithZeroAttendeesWhenTicketsNull() {
        User author = new User();
        author.setId(1L);
        author.setName("Juan");

        Event event = new Event();
        event.setId(11L);
        event.setTitle("Evento");
        event.setDescription("Desc");
        event.setType(EventType.TALLER);
        event.setDate(LocalDate.now().plusDays(5));
        event.setTime(LocalTime.of(17, 0));
        event.setMaxAttendees(100);
        event.setCategory(EventCategory.ONLINE);
        event.setPrice(BigDecimal.TEN);
        event.setAuthor(author);
        event.setTickets(null);
        event.setLocation(null);

        EventResponseDTO response = mapper.toResponse(event);

        assertEquals(0, response.attendeesCount());
    }

    @Test
    void shouldUpdateEntity() {
        Event event = new Event();
        event.setId(99L);
        event.setAuthor(new User());
        event.setTickets(List.of());

        EventRequestDTO dto = new EventRequestDTO(
                "Nuevo titulo",
                "Nueva descripcion",
                EventType.HACKATHON,
                LocalDate.now().plusDays(20),
                LocalTime.of(10, 30),
                80,
                null,
                EventCategory.PRESENCIAL,
                BigDecimal.valueOf(99.99),
                null);

        mapper.updateEntity(dto, event);

        assertEquals("Nuevo titulo", event.getTitle());
        assertEquals("Nueva descripcion", event.getDescription());
        assertEquals(EventType.HACKATHON, event.getType());
        assertEquals(80, event.getMaxAttendees());
        assertEquals(BigDecimal.valueOf(99.99), event.getPrice());
        assertNull(event.getLocation());
    }
}
