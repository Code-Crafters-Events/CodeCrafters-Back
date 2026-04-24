package com.code.crafters.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketCleanupScheduler {
    private final TicketRepository ticketRepository;

    @Value("${ticket.cleanup.hours:24}")
    private int cleanupHours;

    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredTickets() {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info(" INICIANDO LIMPIEZA PROGRAMADA DE TICKETS");
        log.info("═══════════════════════════════════════════════════════════════");

        try {
            long pendingDeleted = cleanupAbandonedPendingTickets();
            long pastEventsDeleted = cleanupAllTicketsForPastEvents();

            log.info("═══════════════════════════════════════════════════════════════");
            log.info(" LIMPIEZA COMPLETADA EXITOSAMENTE");
            log.info(" Tickets PENDING eliminados: {}", pendingDeleted);
            log.info(" Tickets de eventos pasados eliminados: {}", pastEventsDeleted);
            log.info(" Total eliminados: {}", pendingDeleted + pastEventsDeleted);
            log.info("═══════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("ERROR DURANTE LA LIMPIEZA DE TICKETS: {}", e.getMessage(), e);
        }
    }

    private long cleanupAbandonedPendingTickets() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(cleanupHours);
        long deletedCount = ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(
                PaymentStatus.PENDING,
                cutoff);
        if (deletedCount > 0) {
            log.info("Limpieza PENDING: {} tickets eliminados...", deletedCount);
        }
        return deletedCount;
    }

    private long cleanupAllTicketsForPastEvents() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        long deletedCount = ticketRepository.deleteAllTicketsForPastEvents(today, now);
        if (deletedCount > 0) {
            log.info("Limpieza de precisión: {} tickets eliminados...", deletedCount);
        }
        return deletedCount;
    }

    @Scheduled(fixedDelay = 1800000, initialDelay = 60000)
    @Transactional
    public void cleanupExpiredTicketsFrequent() {
        log.debug("Ejecutando limpieza frecuente de tickets...");
    }
}
