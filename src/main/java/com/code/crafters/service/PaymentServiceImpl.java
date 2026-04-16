package com.code.crafters.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

@Service
@RequiredArgsConstructor
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
        if (ticketRepository.existsByUserIdAndEventId(dto.userId(), dto.eventId())) {
            throw new ResourceAlreadyExistsException("Ya estás apuntado a este evento");
        }

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.userId()));
        Event event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + dto.eventId()));

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

            Ticket ticket = ticketRepository.save(
                    ticketMapper.toEntity(user, event, intent.getId(), PaymentStatus.PENDING));

            return paymentMapper.toResponse(intent, event, ticket);

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
        } catch (SignatureVerificationException e) {
            throw new SecurityException("Firma de webhook inválida");
        }

        try {
            switch (stripeEvent.getType()) {
                case "payment_intent.succeeded" -> {
                    PaymentIntent intent = (PaymentIntent) stripeEvent
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();
                    updateTicketStatus(intent.getId(), PaymentStatus.COMPLETED);
                }
                case "payment_intent.payment_failed" -> {
                    PaymentIntent intent = (PaymentIntent) stripeEvent
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();
                    updateTicketStatus(intent.getId(), PaymentStatus.FAILED);
                }
                default -> System.out.println("Evento Stripe ignorado: " + stripeEvent.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error procesando evento Stripe: " + e.getMessage());
        }
    }

    private void updateTicketStatus(String paymentIntentId, PaymentStatus status) {
        ticketRepository.findByPaymentIntentId(paymentIntentId).ifPresent(ticket -> {
            String verificationCode = null;
            String qrUrl = null;

            if (status == PaymentStatus.COMPLETED) {
                verificationCode = UUID.randomUUID().toString();
                qrUrl = qrService.generateTicketQr(ticket.getId(), verificationCode);
            }

            ticketMapper.updateTicketPayment(status, verificationCode, qrUrl, ticket);
            ticketRepository.save(ticket);
        });
    }
}