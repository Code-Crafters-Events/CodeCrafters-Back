package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.code.crafters.config.StripeProperties;
import com.code.crafters.dto.request.PaymentIntentRequestDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.mapper.PaymentMapper;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;

@SuppressWarnings("null")
class PaymentServiceImplAdvancedTest {

        private TicketRepository ticketRepository;
        private UserRepository userRepository;
        private EventRepository eventRepository;
        private StripeProperties stripeProperties;
        private TicketMapper ticketMapper;
        private PaymentMapper paymentMapper;
        private QrService qrService;
        private PaymentServiceImpl service;

        @BeforeEach
        void setUp() {
                ticketRepository = mock(TicketRepository.class);
                userRepository = mock(UserRepository.class);
                eventRepository = mock(EventRepository.class);
                stripeProperties = mock(StripeProperties.class);
                ticketMapper = mock(TicketMapper.class);
                paymentMapper = mock(PaymentMapper.class);
                qrService = mock(QrService.class);

                service = new PaymentServiceImpl(
                                ticketRepository, userRepository, eventRepository,
                                stripeProperties, ticketMapper, paymentMapper, qrService);
        }

        @Test
        @DisplayName("Should process free ticket successfully")
        void shouldProcessFreeTicket() {
                User user = new User();
                user.setId(1L);
                Event freeEvent = new Event();
                freeEvent.setId(10L);
                freeEvent.setPrice(BigDecimal.ZERO);

                Ticket mockTicket = new Ticket();
                mockTicket.setId(55L);

                when(eventRepository.findById(10L)).thenReturn(Optional.of(freeEvent));
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.empty());
                when(ticketMapper.toEntity(any(), any(), any(), any())).thenReturn(mockTicket);
                when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);

                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(1L, 10L);
                assertDoesNotThrow(() -> service.createPaymentIntent(dto));

                verify(qrService).generateTicketQr(anyLong(), anyString());
        }

        @Test
        @DisplayName("Should throw exception if confirmed ticket already exists")
        void shouldThrowIfTicketExists() {
                User user = new User();
                user.setId(1L);
                Event event = new Event();
                event.setId(10L);
                Ticket existing = new Ticket();
                existing.setPaymentStatus(PaymentStatus.COMPLETED);

                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.of(existing));

                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(1L, 10L);
                assertThrows(ResourceAlreadyExistsException.class, () -> service.createPaymentIntent(dto));
        }

        @Test
        @DisplayName("Should create Stripe Intent and save pending ticket")
        void shouldCreateStripeIntent() throws Exception {
                User user = new User();
                user.setId(1L);
                Event paidEvent = new Event();
                paidEvent.setId(20L);
                paidEvent.setPrice(new BigDecimal("50.00"));

                PaymentIntent mockIntent = mock(PaymentIntent.class);
                when(mockIntent.getId()).thenReturn("pi_123");

                when(eventRepository.findById(20L)).thenReturn(Optional.of(paidEvent));
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(1L, 20L)).thenReturn(Optional.empty());
                when(ticketMapper.toEntity(any(), any(), anyString(), any())).thenReturn(new Ticket());

                try (MockedStatic<PaymentIntent> intentMock = Mockito.mockStatic(PaymentIntent.class)) {
                        intentMock.when(() -> PaymentIntent
                                        .create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                                        .thenReturn(mockIntent);

                        PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(1L, 20L);
                        assertDoesNotThrow(() -> service.createPaymentIntent(dto));
                        verify(ticketRepository).save(any(Ticket.class));
                }
        }

        @Test
        @DisplayName("Should handle successful webhook and activate ticket")
        void shouldHandleWebhookSuccess() throws Exception {
                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");

                PaymentIntent intent = mock(PaymentIntent.class);
                Map<String, String> metadata = new HashMap<>();
                metadata.put("userId", "1");
                metadata.put("eventId", "20");
                when(intent.getId()).thenReturn("pi_123");
                when(intent.getMetadata()).thenReturn(metadata);

                com.stripe.model.Event stripeEvent = mock(com.stripe.model.Event.class);
                com.stripe.model.EventDataObjectDeserializer deserializer = mock(
                                com.stripe.model.EventDataObjectDeserializer.class);

                when(stripeEvent.getType()).thenReturn("payment_intent.succeeded");
                when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(intent));
                when(ticketRepository.findByPaymentIntentId("pi_123")).thenReturn(Optional.empty());
                when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
                when(eventRepository.findById(20L)).thenReturn(Optional.of(new Event()));
                when(ticketMapper.toEntity(any(), any(), any(), any())).thenReturn(new Ticket());
                when(ticketRepository.save(any())).thenReturn(new Ticket());

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent(any(), any(), any())).thenReturn(stripeEvent);
                        service.handleWebhookEvent("payload", "sig");
                        verify(ticketMapper).completeTicketData(eq(PaymentStatus.COMPLETED), any());
                }
        }

        @Test
        @DisplayName("Should throw exception when metadata is missing in webhook")
        void shouldThrowWhenMetadataMissing() {
                PaymentIntent intent = mock(PaymentIntent.class);
                when(intent.getMetadata()).thenReturn(new HashMap<>());
                assertThrows(RuntimeException.class, () -> {
                        com.stripe.model.Event stripeEvent = mock(com.stripe.model.Event.class);
                        com.stripe.model.EventDataObjectDeserializer deserializer = mock(
                                        com.stripe.model.EventDataObjectDeserializer.class);
                        when(stripeEvent.getType()).thenReturn("payment_intent.succeeded");
                        when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                        when(deserializer.getObject()).thenReturn(Optional.of(intent));
                        when(ticketRepository.findByPaymentIntentId(any())).thenReturn(Optional.empty());

                        try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                                webhookMock.when(() -> Webhook.constructEvent(any(), any(), any()))
                                                .thenReturn(stripeEvent);
                                when(stripeProperties.getWebhookSecret()).thenReturn("key");
                                service.handleWebhookEvent("p", "s");
                        }
                });
        }

        @Test
        @DisplayName("Should handle StripeException in refund")
        void shouldHandleRefundError() throws Exception {
                try (MockedStatic<com.stripe.model.Refund> refundMock = Mockito
                                .mockStatic(com.stripe.model.Refund.class)) {
                        refundMock.when(() -> Refund.create(any(com.stripe.param.RefundCreateParams.class)))
                                        .thenThrow(new StripeException("Error", null, null, 0) {
                                        });

                        assertThrows(RuntimeException.class, () -> service.refundPayment("pi_123"));
                }
        }

        @Test
        @DisplayName("Should ignore unknown events in switch default")
        void shouldIgnoreUnknownEvents() throws Exception {
                com.stripe.model.Event unknownEvent = mock(com.stripe.model.Event.class);
                when(unknownEvent.getType()).thenReturn("customer.created");

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent(any(), any(), any())).thenReturn(unknownEvent);
                        when(stripeProperties.getWebhookSecret()).thenReturn("key");
                        assertDoesNotThrow(() -> service.handleWebhookEvent("p", "s"));
                }
        }

        @Test
        @DisplayName("Should throw exception when Stripe metadata is missing userId or eventId")
        void shouldThrowWhenMetadataIsMissing() throws Exception {
                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");
                PaymentIntent intentWithMissingMetadata = mock(PaymentIntent.class);
                when(intentWithMissingMetadata.getId()).thenReturn("pi_123");
                when(intentWithMissingMetadata.getMetadata()).thenReturn(new HashMap<>());
                com.stripe.model.Event stripeEvent = mock(com.stripe.model.Event.class);
                com.stripe.model.EventDataObjectDeserializer deserializer = mock(
                                com.stripe.model.EventDataObjectDeserializer.class);
                when(stripeEvent.getType()).thenReturn("payment_intent.succeeded");
                when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(intentWithMissingMetadata));
                when(ticketRepository.findByPaymentIntentId("pi_123")).thenReturn(Optional.empty());
                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                                        .thenReturn(stripeEvent);

                        RuntimeException exception = assertThrows(RuntimeException.class,
                                        () -> service.handleWebhookEvent("payload", "sig"));
                        assertTrue(exception.getMessage()
                                        .contains("Metadata incompleta en el PaymentIntent de Stripe"));
                }
        }

        @Test
        @DisplayName("Should log warning when payment intent fails")
        void shouldLogWarningOnPaymentFailed() throws Exception {
                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");

                PaymentIntent intent = mock(PaymentIntent.class);
                when(intent.getId()).thenReturn("pi_fail_123");

                com.stripe.model.Event stripeEvent = mock(com.stripe.model.Event.class);
                com.stripe.model.EventDataObjectDeserializer deserializer = mock(
                                com.stripe.model.EventDataObjectDeserializer.class);

                when(stripeEvent.getType()).thenReturn("payment_intent.payment_failed");
                when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(intent));

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent(any(), any(), any())).thenReturn(stripeEvent);
                        assertDoesNotThrow(() -> service.handleWebhookEvent("payload", "sig"));
                }
        }

        @Test
        @DisplayName("Should throw exception when userId is missing in metadata")
        void shouldThrowWhenUserIdMissing() throws Exception {
                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");

                PaymentIntent intent = mock(PaymentIntent.class);
                Map<String, String> metadata = new HashMap<>();
                metadata.put("eventId", "20");
                when(intent.getMetadata()).thenReturn(metadata);

                com.stripe.model.Event stripeEvent = mock(com.stripe.model.Event.class);
                com.stripe.model.EventDataObjectDeserializer deserializer = mock(
                                com.stripe.model.EventDataObjectDeserializer.class);
                when(stripeEvent.getType()).thenReturn("payment_intent.succeeded");
                when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(intent));
                when(ticketRepository.findByPaymentIntentId(any())).thenReturn(Optional.empty());

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent(any(), any(), any())).thenReturn(stripeEvent);

                        RuntimeException ex = assertThrows(RuntimeException.class,
                                        () -> service.handleWebhookEvent("p", "s"));
                        assertTrue(ex.getMessage().contains("Metadata incompleta"));
                }
        }

        @Test
        @DisplayName("Should throw exception when eventId is missing in metadata")
        void shouldThrowWhenEventIdMissing() throws Exception {
                when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test");

                PaymentIntent intent = mock(PaymentIntent.class);
                Map<String, String> metadata = new HashMap<>();
                metadata.put("userId", "1");
                when(intent.getMetadata()).thenReturn(metadata);

                com.stripe.model.Event stripeEvent = mock(com.stripe.model.Event.class);
                com.stripe.model.EventDataObjectDeserializer deserializer = mock(
                                com.stripe.model.EventDataObjectDeserializer.class);
                when(stripeEvent.getType()).thenReturn("payment_intent.succeeded");
                when(stripeEvent.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(intent));
                when(ticketRepository.findByPaymentIntentId(any())).thenReturn(Optional.empty());

                try (MockedStatic<Webhook> webhookMock = Mockito.mockStatic(Webhook.class)) {
                        webhookMock.when(() -> Webhook.constructEvent(any(), any(), any())).thenReturn(stripeEvent);

                        RuntimeException ex = assertThrows(RuntimeException.class,
                                        () -> service.handleWebhookEvent("p", "s"));
                        assertTrue(ex.getMessage().contains("Metadata incompleta"));
                }
        }

        @Test
        @DisplayName("Should handle exception in cleanup job")
        void shouldHandleCleanupException() {
                when(ticketRepository.deleteByPaymentStatusAndCreatedAtBefore(any(), any()))
                                .thenThrow(new RuntimeException("DB Error"));
                assertDoesNotThrow(() -> service.cleanupAbandonedPendingTickets());
        }

        
}