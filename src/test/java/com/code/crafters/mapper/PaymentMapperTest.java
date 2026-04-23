package com.code.crafters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.code.crafters.dto.response.PaymentIntentResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.stripe.model.PaymentIntent;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("PaymentMapper Tests")
class PaymentMapperTest {

    private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void shouldMapStripeIntentAndTicketToResponse() {
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getClientSecret()).thenReturn("secret_123");
        when(intent.getId()).thenReturn("pi_123");

        Event event = new Event();
        event.setPrice(BigDecimal.valueOf(49.99));

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setQrUrl("qr.png");
        ticket.setVerificationCode("code-123");

        PaymentIntentResponseDTO response = mapper.toResponse(intent, event, ticket);

        assertEquals("secret_123", response.clientSecret());
        assertEquals("pi_123", response.paymentIntentId());
        assertEquals(BigDecimal.valueOf(49.99), response.amount());
        assertEquals("eur", response.currency());
        assertEquals(1L, response.ticketId());
    }

    @Test
    void shouldMapFreeResponse() {
        Ticket ticket = new Ticket();
        ticket.setId(2L);
        ticket.setQrUrl("qr-free.png");
        ticket.setVerificationCode("free-code");

        PaymentIntentResponseDTO response = mapper.toFreeResponse(ticket, BigDecimal.ZERO);

        assertNull(response.clientSecret());
        assertNull(response.paymentIntentId());
        assertEquals(BigDecimal.ZERO, response.amount());
        assertEquals("eur", response.currency());
        assertEquals(2L, response.ticketId());
    }

    @Test
    void shouldMapStripeIntentWithoutTicketData() {
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getClientSecret()).thenReturn("secret_999");
        when(intent.getId()).thenReturn("pi_999");

        Event event = new Event();
        event.setPrice(BigDecimal.valueOf(19.99));

        PaymentIntentResponseDTO response = mapper.toPaymentIntentResponse(intent, event);

        assertEquals("secret_999", response.clientSecret());
        assertEquals("pi_999", response.paymentIntentId());
        assertEquals(BigDecimal.valueOf(19.99), response.amount());
        assertEquals("eur", response.currency());
        assertNull(response.ticketId());
    }
}
