package com.code.crafters.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.service.PaymentService;
import com.stripe.model.PaymentIntent;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("PaymentReconciliationScheduler Tests")
class PaymentReconciliationSchedulerTest {

    @Test
    void shouldDoNothingWhenNoPendingTickets() {
        TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
        PaymentService paymentService = Mockito.mock(PaymentService.class);

        PaymentReconciliationScheduler scheduler = new PaymentReconciliationScheduler(ticketRepository, paymentService);

        when(ticketRepository.findByPaymentStatus(PaymentStatus.PENDING)).thenReturn(List.of());

        scheduler.reconcilePendingTickets();

        verify(paymentService, never()).activateTicket(Mockito.any());
    }

    @Test
    void shouldSkipPendingTicketWithoutPaymentIntentId() {
        TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
        PaymentService paymentService = Mockito.mock(PaymentService.class);

        PaymentReconciliationScheduler scheduler = new PaymentReconciliationScheduler(ticketRepository, paymentService);

        Ticket ticket = new Ticket();
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setPaymentIntentId(null);

        when(ticketRepository.findByPaymentStatus(PaymentStatus.PENDING)).thenReturn(List.of(ticket));

        scheduler.reconcilePendingTickets();

        verify(paymentService, never()).activateTicket(Mockito.any());
    }

    @Test
    void shouldActivateTicketWhenStripeIntentSucceeded() {
        TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
        PaymentService paymentService = Mockito.mock(PaymentService.class);

        PaymentReconciliationScheduler scheduler = new PaymentReconciliationScheduler(ticketRepository, paymentService);

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setPaymentIntentId("pi_123");

        PaymentIntent intent = Mockito.mock(PaymentIntent.class);

        when(ticketRepository.findByPaymentStatus(PaymentStatus.PENDING)).thenReturn(List.of(ticket));
        when(intent.getStatus()).thenReturn("succeeded");

        try (MockedStatic<PaymentIntent> mocked = Mockito.mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.retrieve("pi_123")).thenReturn(intent);

            scheduler.reconcilePendingTickets();
        }

        verify(paymentService).activateTicket(ticket);
    }

    @Test
    void shouldIgnoreStripeErrors() {
        TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
        PaymentService paymentService = Mockito.mock(PaymentService.class);

        PaymentReconciliationScheduler scheduler = new PaymentReconciliationScheduler(ticketRepository, paymentService);

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setPaymentIntentId("pi_123");

        when(ticketRepository.findByPaymentStatus(PaymentStatus.PENDING)).thenReturn(List.of(ticket));

        try (MockedStatic<PaymentIntent> mocked = Mockito.mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.retrieve("pi_123"))
                    .thenThrow(new RuntimeException("stripe error"));

            scheduler.reconcilePendingTickets();
        }

        verify(paymentService, never()).activateTicket(ticket);
    }

    @Test
    void shouldSkipPendingTicketWithEmptyPaymentIntentId() {
        TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
        PaymentService paymentService = Mockito.mock(PaymentService.class);

        PaymentReconciliationScheduler scheduler = new PaymentReconciliationScheduler(
                ticketRepository, paymentService);

        Ticket ticket = new Ticket();
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setPaymentIntentId("");

        when(ticketRepository.findByPaymentStatus(PaymentStatus.PENDING))
                .thenReturn(List.of(ticket));

        scheduler.reconcilePendingTickets();

        verify(paymentService, never()).activateTicket(Mockito.any());
    }

    @Test
    void shouldNotActivateTicketWhenStripeStatusIsNotSucceeded() {
        TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
        PaymentService paymentService = Mockito.mock(PaymentService.class);

        PaymentReconciliationScheduler scheduler = new PaymentReconciliationScheduler(
                ticketRepository, paymentService);

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setPaymentIntentId("pi_123");

        PaymentIntent intent = Mockito.mock(PaymentIntent.class);

        when(ticketRepository.findByPaymentStatus(PaymentStatus.PENDING))
                .thenReturn(List.of(ticket));
        when(intent.getStatus()).thenReturn("processing");

        try (MockedStatic<PaymentIntent> mocked = Mockito.mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.retrieve("pi_123")).thenReturn(intent);

            scheduler.reconcilePendingTickets();
        }

        verify(paymentService, never()).activateTicket(ticket);
    }
}
