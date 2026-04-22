package com.code.crafters.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.crafters.config.StripeProperties;
import com.code.crafters.dto.request.PaymentIntentRequestDTO;
import com.code.crafters.dto.response.PaymentIntentResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.PaymentMapper;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class PaymentServiceImpl implements PaymentService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final StripeProperties stripeProperties;
    private final TicketMapper ticketMapper;
    private final PaymentMapper paymentMapper;
    private final QrService qrService;

    private static final String CURRENCY = "eur";

    @Override
    @Transactional
    public PaymentIntentResponseDTO createPaymentIntent(PaymentIntentRequestDTO dto) {
        Event event = findEventOrThrow(dto.eventId());
        User user = findUserOrThrow(dto.userId());

        checkAndCleanupExistingTicket(dto);

        if (event.getPrice().compareTo(BigDecimal.ZERO) == 0)
            return processFreeTicket(user, event);

        return createStripeIntent(event, user);
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) {
        com.stripe.model.Event stripeEvent = constructStripeEvent(payload, sigHeader);

        log.info("Webhook recibido. Tipo: {}", stripeEvent.getType());

        try {
            switch (stripeEvent.getType()) {
                case "payment_intent.succeeded" ->
                    extractIntent(stripeEvent).ifPresent(this::createTicketOnPaymentSuccess);

                case "payment_intent.payment_failed" ->
                    extractIntent(stripeEvent).ifPresent(intent -> log.warn("Pago fallido: {}", intent.getId()));

                default -> log.info("Evento ignorado: {}", stripeEvent.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage());
            throw new RuntimeException("Error en webhook: " + e.getMessage());
        }
    }

    @Transactional
    private void createTicketOnPaymentSuccess(PaymentIntent intent) {
        Ticket ticket = ticketRepository.findByPaymentIntentId(intent.getId())
                .orElseGet(() -> createTicketFromMetadata(intent));

        activateTicket(ticket);
    }

    @Override
    @Transactional
    public void refundPayment(String paymentIntentId) {
        try {
            log.info("Iniciando reembolso para: {}", paymentIntentId);

            com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            com.stripe.model.Refund.create(params);

            log.info("Reembolso procesado exitosamente");
        } catch (StripeException e) {
            log.error("Error en reembolso Stripe: {}", e.getMessage());
            throw new RuntimeException("No se pudo procesar la devolución: " + e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupAbandonedPendingTickets() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
            long deleted = ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(
                    PaymentStatus.PENDING, cutoff);
            log.info("{} tickets PENDING eliminados", deleted);
        } catch (Exception e) {
            log.error("Error al limpiar tickets PENDING: {}", e.getMessage());
        }
    }

    public void activateTicket(Ticket ticket) {
        if (ticket.getPaymentStatus() != PaymentStatus.COMPLETED) {
            ticketMapper.completeTicketData(PaymentStatus.COMPLETED, ticket);
            ticket = ticketRepository.save(ticket);
            String qrUrl = qrService.generateTicketQr(ticket.getId(), ticket.getVerificationCode());
            ticket.setQrUrl(qrUrl);
            ticketRepository.save(ticket);
        }
    }

    private void savePendingTicket(User user, Event event, String intentId) {
        Ticket ticket = ticketMapper.toEntity(user, event, intentId, PaymentStatus.PENDING);
        ticket.setPaymentIntentId(intentId);
        ticketRepository.save(ticket);
    }

    private PaymentIntentCreateParams createStripeParams(Event event, User user) {
        return PaymentIntentCreateParams.builder()
                .setAmount(event.getPrice().multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency(CURRENCY)
                .putMetadata("userId", String.valueOf(user.getId()))
                .putMetadata("eventId", String.valueOf(event.getId()))
                .build();
    }

    private void checkAndCleanupExistingTicket(PaymentIntentRequestDTO dto) {
        ticketRepository.findByUserIdAndEventId(dto.userId(), dto.eventId())
                .ifPresent(existingTicket -> {
                    PaymentStatus status = existingTicket.getPaymentStatus();
                    if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FREE) {
                        throw new ResourceAlreadyExistsException("Ya tienes una entrada confirmada para este evento");
                    }
                    ticketRepository.delete(existingTicket);
                    ticketRepository.flush();
                });
    }

    private PaymentIntentResponseDTO processFreeTicket(User user, Event event) {
        Ticket ticket = ticketMapper.toEntity(user, event, null, PaymentStatus.FREE);
        ticket.setVerificationCode(UUID.randomUUID().toString());
        ticket = ticketRepository.save(ticket);

        String qrUrl = qrService.generateTicketQr(ticket.getId(), ticket.getVerificationCode());
        ticket.setQrUrl(qrUrl);
        ticket = ticketRepository.save(ticket);

        return paymentMapper.toFreeResponse(ticket, BigDecimal.ZERO);
    }

    private Ticket createTicketFromMetadata(PaymentIntent intent) {
        String userId = intent.getMetadata().get("userId");
        String eventId = intent.getMetadata().get("eventId");

        if (userId == null || eventId == null) {
            throw new RuntimeException("Metadata incompleta en el PaymentIntent de Stripe");
        }

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Event event = eventRepository.findById(Long.parseLong(eventId))
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        return ticketMapper.toEntity(user, event, intent.getId(), PaymentStatus.PENDING);
    }

    private PaymentIntentResponseDTO createStripeIntent(Event event, User user) {
        try {
            PaymentIntent intent = PaymentIntent.create(createStripeParams(event, user));
            savePendingTicket(user, event, intent.getId());
            return paymentMapper.toPaymentIntentResponse(intent, event);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private com.stripe.model.Event constructStripeEvent(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Firma de webhook inválida");
            throw new SecurityException("Firma de webhook inválida");
        }
    }

    private Optional<PaymentIntent> extractIntent(com.stripe.model.Event stripeEvent) {
        return stripeEvent.getDataObjectDeserializer().getObject()
                .map(obj -> (PaymentIntent) obj);
    }
}
