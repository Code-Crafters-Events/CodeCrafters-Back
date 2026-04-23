package com.code.crafters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.entity.enums.PaymentStatus;

@SpringBootTest(classes = {
        PageMapperImpl.class,
        EventMapperImpl.class,
        TicketMapperImpl.class,
        LocationMapperImpl.class
})
@SuppressWarnings("null")
@DisplayName("PageMapper Tests")
class PageMapperTest {

    @Autowired
    private PageMapper mapper;

    @Test
    void shouldMapEventPage() {
        User author = new User();
        author.setId(1L);
        author.setName("Juan");
        author.setAlias("juanp");

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Workshop");
        event.setDescription("Desc");
        event.setType(EventType.TALLER);
        event.setDate(LocalDate.now().plusDays(2));
        event.setTime(LocalTime.of(18, 0));
        event.setMaxAttendees(20);
        event.setCategory(EventCategory.PRESENCIAL);
        event.setPrice(BigDecimal.TEN);
        event.setAuthor(author);
        event.setTickets(List.of());
        event.setLocation(null);

        Page<Event> page = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1);

        PageResponseDTO<EventResponseDTO> result = mapper.toEventPageResponse(page);

        assertEquals(1, result.content().size());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalElements());
    }

    @Test
    void shouldMapTicketPage() {
        User user = new User();
        user.setId(1L);
        user.setName("Juan");

        Event event = new Event();
        event.setId(2L);
        event.setTitle("Evento");

        Ticket ticket = new Ticket();
        ticket.setId(3L);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setVerificationCode("code-1");

        Page<Ticket> page = new PageImpl<>(List.of(ticket), PageRequest.of(0, 10), 1);

        PageResponseDTO<TicketResponseDTO> result = mapper.toTicketPageResponse(page);

        assertEquals(1, result.content().size());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalElements());
    }
}
