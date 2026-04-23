package com.code.crafters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("TicketMapper Tests")
class TicketMapperTest {

    private final TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @Test
    void shouldMapTicketToResponse() {
        User user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setProfileImage("avatar.png");

        Event event = new Event();
        event.setId(2L);
        event.setTitle("Workshop");

        Ticket ticket = new Ticket();
        ticket.setId(3L);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setPaymentStatus(PaymentStatus.COMPLETED);
        ticket.setPaymentIntentId("pi_123");
        ticket.setQrUrl("qr.png");
        ticket.setVerificationCode("code-123");

        TicketResponseDTO response = mapper.toResponse(ticket);

        assertEquals(1L, response.userId());
        assertEquals("Juan", response.userName());
        assertEquals(2L, response.eventId());
        assertEquals("Workshop", response.eventTitle());
        assertEquals("code-123", response.verificationCode());
    }

    @Test
    void shouldCreateTicketEntity() {
        User user = new User();
        Event event = new Event();

        Ticket ticket = mapper.toEntity(user, event, "pi_123", PaymentStatus.PENDING);

        assertEquals(user, ticket.getUser());
        assertEquals(event, ticket.getEvent());
        assertEquals(PaymentStatus.PENDING, ticket.getPaymentStatus());
        assertEquals("pi_123", ticket.getPaymentIntentId());
        assertNotNull(ticket.getCreatedAt());
    }

    @Test
    void shouldUpdateTicketPaymentFields() {
        Ticket ticket = new Ticket();

        mapper.updateTicketPayment(PaymentStatus.COMPLETED, "code-1", "qr-1", ticket);

        assertEquals(PaymentStatus.COMPLETED, ticket.getPaymentStatus());
        assertEquals("code-1", ticket.getVerificationCode());
        assertEquals("qr-1", ticket.getQrUrl());
    }

    @Test
    void shouldReturnNotFoundVerificationResponse() {
        TicketVerificationResponseDTO response = mapper.toNotFoundResponse("Ticket no encontrado");

        assertFalse(response.valid());
        assertEquals("Ticket no encontrado", response.message());
        assertNull(response.ticketId());
    }

    @Test
    void shouldMapVerificationResponse() {
        User user = new User();
        user.setName("Juan");

        Event event = new Event();
        event.setTitle("Workshop");

        Ticket ticket = new Ticket();
        ticket.setId(7L);
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUsedAt(null);
        ticket.setPaymentStatus(PaymentStatus.COMPLETED);

        TicketVerificationResponseDTO response = mapper.toVerificationResponse(ticket, true, "Ticket valido");

        assertTrue(response.valid());
        assertEquals(7L, response.ticketId());
        assertEquals("Workshop", response.eventTitle());
        assertEquals("Juan", response.userName());
        assertEquals(PaymentStatus.COMPLETED, response.paymentStatus());
    }

    @Test
    void shouldCompleteTicketData() {
        Ticket ticket = new Ticket();

        mapper.completeTicketData(PaymentStatus.COMPLETED, ticket);

        assertEquals(PaymentStatus.COMPLETED, ticket.getPaymentStatus());
        assertNotNull(ticket.getVerificationCode());
        assertNull(ticket.getQrUrl());
    }
}
