package com.code.crafters.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("TicketCleanupScheduler Tests")
class TicketCleanupSchedulerTest {

    private TicketRepository ticketRepository;
    private TicketCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        ticketRepository = Mockito.mock(TicketRepository.class);
        scheduler = new TicketCleanupScheduler(ticketRepository);
        ReflectionTestUtils.setField(scheduler, "cleanupHours", 24);
    }

    @Test
    void shouldRunCleanupSuccessfully() {
        when(ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(
                org.mockito.Mockito.eq(PaymentStatus.PENDING),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(2L);

        when(ticketRepository.deleteAllTicketsForPastEvents(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(3L);

        assertDoesNotThrow(() -> scheduler.cleanupExpiredTickets());
    }

    @Test
    void shouldHandlePendingCleanupError() {
        when(ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(
                org.mockito.Mockito.eq(PaymentStatus.PENDING),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("db error"));

        when(ticketRepository.deleteAllTicketsForPastEvents(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(1L);

        assertDoesNotThrow(() -> scheduler.cleanupExpiredTickets());
    }

    @Test
    void shouldHandlePastEventsCleanupError() {
        when(ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(
                org.mockito.Mockito.eq(PaymentStatus.PENDING),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(1L);

        when(ticketRepository.deleteAllTicketsForPastEvents(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("db error"));

        assertDoesNotThrow(() -> scheduler.cleanupExpiredTickets());
    }

    @Test
    void shouldRunFrequentCleanupWithoutErrors() {
        assertDoesNotThrow(() -> scheduler.cleanupExpiredTicketsFrequent());
    }
}
