package com.code.crafters.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.userId()));
        Event event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + dto.eventId()));

        Optional<Ticket> existingTicket = ticketRepository.findByUserIdAndEventId(dto.userId(), dto.eventId());
        if (existingTicket.isPresent()) {
            PaymentStatus status = existingTicket.get().getPaymentStatus();
            if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FREE) {
                throw new ResourceAlreadyExistsException("Ya tienes una entrada confirmada para este evento");
            }
            ticketRepository.delete(existingTicket.get());
            ticketRepository.flush();
        }

        if (event.getPrice() == null || event.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            Ticket ticket = ticketMapper.toEntity(user, event, null, PaymentStatus.FREE);
            String verificationCode = UUID.randomUUID().toString();
            ticket.setVerificationCode(verificationCode);
            ticket = ticketRepository.save(ticket);
            String qrUrl = qrService.generateTicketQr(ticket.getId(), verificationCode);
            ticket.setQrUrl(qrUrl);
            ticket = ticketRepository.save(ticket);
            return paymentMapper.toFreeResponse(ticket, BigDecimal.ZERO);
        }
        try {
            long amountInCents = event.getPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", String.valueOf(dto.userId()));
            metadata.put("eventId", String.valueOf(dto.eventId()));

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(CURRENCY)
                    .setDescription("Entrada para: " + event.getTitle())
                    .putAllMetadata(metadata)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return paymentMapper.toPaymentIntentResponse(intent, event);

        } catch (StripeException e) {
            throw new RuntimeException("Error al crear el pago con Stripe: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) {
        com.stripe.model.Event stripeEvent;

        try {
            stripeEvent = Webhook.constructEvent(
                    payload, sigHeader, stripeProperties.getWebhookSecret());
            log.info("Webhook verificado correctamente. Tipo: {}", stripeEvent.getType());
        } catch (SignatureVerificationException e) {
            log.error("Firma de webhook inválida: {}", e.getMessage());
            throw new SecurityException("Firma de webhook inválida");
        }

        try {
            switch (stripeEvent.getType()) {
                case "payment_intent.succeeded" -> {
                    log.info("payment_intent.succeeded recibido");
                    PaymentIntent intent = (PaymentIntent) stripeEvent
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();
                    createTicketOnPaymentSuccess(intent);
                }
                case "payment_intent.payment_failed" -> {
                    log.warn("payment_intent.payment_failed recibido");
                    PaymentIntent intent = (PaymentIntent) stripeEvent
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();
                    log.warn("Pago fallido para PaymentIntent: {}", intent.getId());
                }
                default -> log.info("Evento Stripe ignorado: {}", stripeEvent.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando evento Stripe: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando evento Stripe: " + e.getMessage());
        }
    }

    @Transactional
    private void createTicketOnPaymentSuccess(PaymentIntent intent) {
        try {
            log.info("Webhook recibido - PaymentIntent ID: {}", intent.getId());
            log.info("Metadata completa: {}", intent.getMetadata());

            String userId = intent.getMetadata().get("userId");
            String eventId = intent.getMetadata().get("eventId");

            log.info("userId: {}, eventId: {}", userId, eventId);

            if (userId == null || eventId == null) {
                log.error("Metadata incompleta en PaymentIntent: {}", intent.getId());
                return;
            }

            User user = userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
            Event event = eventRepository.findById(Long.parseLong(eventId))
                    .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventId));

            log.info("Usuario encontrado: {}, Evento encontrado: {}", user.getId(), event.getId());

            Ticket ticket = ticketMapper.toEntity(user, event, intent.getId(), PaymentStatus.COMPLETED);

            String verificationCode = UUID.randomUUID().toString();
            ticket.setVerificationCode(verificationCode);

            ticket = ticketRepository.save(ticket);
            log.info("Ticket guardado con ID: {}", ticket.getId());

            String qrUrl = qrService.generateTicketQr(ticket.getId(), verificationCode);
            ticket.setQrUrl(qrUrl);

            ticket = ticketRepository.save(ticket);

            log.info("Ticket creado exitosamente para usuario {} en evento {} - QR URL: {}", userId, eventId, qrUrl);
        } catch (Exception e) {
            log.error("Error al crear ticket en webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear ticket: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void refundPayment(String paymentIntentId) {
        try {
            log.info("Iniciando reembolso para PaymentIntent: {}", paymentIntentId);
            com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(params);
            log.info("Reembolso procesado exitosamente en Stripe. ID: {}", refund.getId());
        } catch (StripeException e) {
            log.error("Error de Stripe al procesar reembolso: {}", e.getMessage());
            throw new RuntimeException(
                    "No se pudo procesar la devolución con el proveedor de pagos: " + e.getMessage());
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
}
