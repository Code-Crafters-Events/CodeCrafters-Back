package com.code.crafters.scheduler;

import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.service.PaymentService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationScheduler {

    private final TicketRepository ticketRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 120000, initialDelay = 5000)
    @Transactional
    public void reconcilePendingTickets() {
        log.info("Checking for tickets marked as PENDING that might have been paid...");

        List<Ticket> pendingTickets = ticketRepository.findByPaymentStatus(PaymentStatus.PENDING);

        if (pendingTickets.isEmpty()) {
            return;
        }

        for (Ticket ticket : pendingTickets) {
            if (ticket.getPaymentIntentId() == null || ticket.getPaymentIntentId().isEmpty()) {
                continue;
            }

            try {
                PaymentIntent intent = PaymentIntent.retrieve(ticket.getPaymentIntentId());

                if ("succeeded".equals(intent.getStatus())) {
                    log.info("¡Ticket rescatado! El pago {} ya estaba pagado en Stripe. Activando ticket...",
                            ticket.getId());
                    paymentService.activateTicket(ticket);
                }
            } catch (Exception e) {
                log.error("Error al consultar Stripe para el ticket {}: {}", ticket.getId(), e.getMessage());
            }
        }
    }
}