package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.code.crafters.config.StripeProperties;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.mapper.PaymentMapper;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;

@SuppressWarnings("null")
@DisplayName("PaymentServiceImpl Advanced Tests")
class PaymentServiceImplAdvancedTest {

        @Test
        void shouldRefundPaymentSuccessfully() throws Exception {
                TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
                UserRepository userRepository = Mockito.mock(UserRepository.class);
                EventRepository eventRepository = Mockito.mock(EventRepository.class);
                StripeProperties stripeProperties = Mockito.mock(StripeProperties.class);
                TicketMapper ticketMapper = Mockito.mock(TicketMapper.class);
                PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);
                QrService qrService = Mockito.mock(QrService.class);

                PaymentServiceImpl service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);

                try (MockedStatic<Refund> refundMock = Mockito.mockStatic(Refund.class)) {
                        refundMock.when(() -> Refund.create(Mockito.any(RefundCreateParams.class)))
                                        .thenReturn(Mockito.mock(Refund.class));

                        assertDoesNotThrow(() -> service.refundPayment("pi_123"));
                }
        }

        @Test
        void shouldThrowWhenRefundFails() throws Exception {
                TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
                UserRepository userRepository = Mockito.mock(UserRepository.class);
                EventRepository eventRepository = Mockito.mock(EventRepository.class);
                StripeProperties stripeProperties = Mockito.mock(StripeProperties.class);
                TicketMapper ticketMapper = Mockito.mock(TicketMapper.class);
                PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);
                QrService qrService = Mockito.mock(QrService.class);

                PaymentServiceImpl service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);

                StripeException stripeException = new StripeException("refund error", null, null, 0) {
                };

                try (MockedStatic<Refund> refundMock = Mockito.mockStatic(Refund.class)) {
                        refundMock.when(() -> Refund.create(Mockito.any(RefundCreateParams.class)))
                                        .thenThrow(stripeException);

                        assertThrows(RuntimeException.class, () -> service.refundPayment("pi_123"));
                }
        }

        @Test
        void shouldDeleteAbandonedPendingTickets() {
                TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
                UserRepository userRepository = Mockito.mock(UserRepository.class);
                EventRepository eventRepository = Mockito.mock(EventRepository.class);
                StripeProperties stripeProperties = Mockito.mock(StripeProperties.class);
                TicketMapper ticketMapper = Mockito.mock(TicketMapper.class);
                PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);
                QrService qrService = Mockito.mock(QrService.class);

                PaymentServiceImpl service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);

                when(ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(
                                Mockito.eq(PaymentStatus.PENDING),
                                Mockito.any(LocalDateTime.class)))
                                .thenReturn(3L);

                assertDoesNotThrow(service::cleanupAbandonedPendingTickets);
        }

        @Test
        void shouldIgnoreCleanupErrors() {
                TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
                UserRepository userRepository = Mockito.mock(UserRepository.class);
                EventRepository eventRepository = Mockito.mock(EventRepository.class);
                StripeProperties stripeProperties = Mockito.mock(StripeProperties.class);
                TicketMapper ticketMapper = Mockito.mock(TicketMapper.class);
                PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);
                QrService qrService = Mockito.mock(QrService.class);

                PaymentServiceImpl service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);

                when(ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(
                                Mockito.eq(PaymentStatus.PENDING),
                                Mockito.any(LocalDateTime.class)))
                                .thenThrow(new RuntimeException("db error"));

                assertDoesNotThrow(service::cleanupAbandonedPendingTickets);
        }

        @Test
        void shouldHandleWebhookSucceededEvent() throws Exception {
                TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
                UserRepository userRepository = Mockito.mock(UserRepository.class);
                EventRepository eventRepository = Mockito.mock(EventRepository.class);
                StripeProperties stripeProperties = Mockito.mock(StripeProperties.class);
                TicketMapper ticketMapper = Mockito.mock(TicketMapper.class);
                PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);
                QrService qrService = Mockito.mock(QrService.class);

                PaymentServiceImpl service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);

                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");

                Ticket ticket = new Ticket();
                ticket.setId(1L);
                ticket.setPaymentStatus(PaymentStatus.PENDING);
                ticket.setVerificationCode("code-123");

                PaymentIntent intent = Mockito.mock(PaymentIntent.class);
                when(intent.getId()).thenReturn("pi_123");

                com.stripe.model.Event stripeEvent = Mockito.mock(com.stripe.model.Event.class);
                com.stripe.model.EventDataObjectDeserializer deserializer = Mockito
                                .mock(com.stripe.model.EventDataObjectDeserializer.class);

                when(stripeEvent.getType()).thenReturn("payment_intent.succeeded");
                when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(intent));
                when(ticketRepository.findByPaymentIntentId("pi_123")).thenReturn(Optional.of(ticket));
                when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(qrService.generateTicketQr(1L, "code-123"))
                                .thenReturn("http://localhost:8080/uploads/qr/test.png");

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test"))
                                        .thenReturn(stripeEvent);

                        assertDoesNotThrow(() -> service.handleWebhookEvent("payload", "sig"));
                }

                verify(ticketMapper).completeTicketData(PaymentStatus.COMPLETED, ticket);
                verify(ticketRepository, Mockito.times(2)).save(any(Ticket.class));
                verify(qrService).generateTicketQr(1L, "code-123");
        }

        @Test
        void shouldHandleWebhookFailedEventWithoutThrowing() throws Exception {
                TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
                UserRepository userRepository = Mockito.mock(UserRepository.class);
                EventRepository eventRepository = Mockito.mock(EventRepository.class);
                StripeProperties stripeProperties = Mockito.mock(StripeProperties.class);
                TicketMapper ticketMapper = Mockito.mock(TicketMapper.class);
                PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);
                QrService qrService = Mockito.mock(QrService.class);

                PaymentServiceImpl service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);

                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");

                PaymentIntent intent = Mockito.mock(PaymentIntent.class);
                com.stripe.model.Event stripeEvent = Mockito.mock(com.stripe.model.Event.class);
                com.stripe.model.EventDataObjectDeserializer deserializer = Mockito
                                .mock(com.stripe.model.EventDataObjectDeserializer.class);

                when(stripeEvent.getType()).thenReturn("payment_intent.payment_failed");
                when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(intent));

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test"))
                                        .thenReturn(stripeEvent);

                        assertDoesNotThrow(() -> service.handleWebhookEvent("payload", "sig"));
                }
        }

        @Test
        void shouldThrowSecurityExceptionWhenWebhookSignatureIsInvalid() throws Exception {
                TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
                UserRepository userRepository = Mockito.mock(UserRepository.class);
                EventRepository eventRepository = Mockito.mock(EventRepository.class);
                StripeProperties stripeProperties = Mockito.mock(StripeProperties.class);
                TicketMapper ticketMapper = Mockito.mock(TicketMapper.class);
                PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);
                QrService qrService = Mockito.mock(QrService.class);

                PaymentServiceImpl service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);

                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test"))
                                        .thenThrow(new SignatureVerificationException("invalid", "sig"));

                        assertThrows(SecurityException.class, () -> service.handleWebhookEvent("payload", "sig"));
                }
        }
}
