package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Unit Tests")
class PaymentServiceImplTest {

        @Mock
        private TicketRepository ticketRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private EventRepository eventRepository;

        @Mock
        private StripeProperties stripeProperties;

        @Mock
        private TicketMapper ticketMapper;

        @Mock
        private PaymentMapper paymentMapper;

        @Mock
        private QrService qrService;

        @InjectMocks
        private PaymentServiceImpl paymentService;

        private User user;
        private Event freeEvent;
        private Event paidEvent;
        private Ticket pendingTicket;
        private Ticket freeTicket;

        @BeforeEach
        void setUp() {
                user = new User();
                user.setId(1L);
                user.setEmail("juan@example.com");

                freeEvent = new Event();
                freeEvent.setId(10L);
                freeEvent.setPrice(BigDecimal.ZERO);

                paidEvent = new Event();
                paidEvent.setId(20L);
                paidEvent.setPrice(BigDecimal.valueOf(49.99));

                pendingTicket = new Ticket();
                pendingTicket.setId(100L);
                pendingTicket.setUser(user);
                pendingTicket.setEvent(paidEvent);
                pendingTicket.setPaymentStatus(PaymentStatus.PENDING);
                pendingTicket.setCreatedAt(LocalDateTime.now());

                freeTicket = new Ticket();
                freeTicket.setId(200L);
                freeTicket.setUser(user);
                freeTicket.setEvent(freeEvent);
                freeTicket.setPaymentStatus(PaymentStatus.FREE);
                freeTicket.setVerificationCode(UUID.randomUUID().toString());
        }

        @Test
        @DisplayName("Should create free ticket payment intent response successfully")
        void shouldCreateFreeTicketPaymentIntentResponseSuccessfully() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), freeEvent.getId());
                PaymentIntentResponseDTO responseDTO = new PaymentIntentResponseDTO(
                                null,
                                null,
                                BigDecimal.ZERO,
                                "eur",
                                200L,
                                "http://localhost:8080/uploads/qr/test.png",
                                freeTicket.getVerificationCode());

                when(eventRepository.findById(freeEvent.getId())).thenReturn(Optional.of(freeEvent));
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(user.getId(), freeEvent.getId()))
                                .thenReturn(Optional.empty());
                when(ticketMapper.toEntity(user, freeEvent, null, PaymentStatus.FREE)).thenReturn(freeTicket);
                when(ticketRepository.save(any(Ticket.class))).thenReturn(freeTicket);
                when(qrService.generateTicketQr(eq(freeTicket.getId()), anyString()))
                                .thenReturn("http://localhost:8080/uploads/qr/test.png");
                when(paymentMapper.toFreeResponse(freeTicket, BigDecimal.ZERO)).thenReturn(responseDTO);

                PaymentIntentResponseDTO result = paymentService.createPaymentIntent(dto);

                assertNotNull(result);
                assertEquals("eur", result.currency());
                assertEquals(200L, result.ticketId());
                verify(ticketRepository, org.mockito.Mockito.times(2)).save(any(Ticket.class));
                verify(qrService).generateTicketQr(eq(freeTicket.getId()), anyString());
        }

        @Test
        @DisplayName("Should reject when confirmed ticket already exists")
        void shouldRejectWhenConfirmedTicketAlreadyExists() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), freeEvent.getId());

                Ticket existing = new Ticket();
                existing.setPaymentStatus(PaymentStatus.COMPLETED);

                when(eventRepository.findById(freeEvent.getId())).thenReturn(Optional.of(freeEvent));
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(user.getId(), freeEvent.getId()))
                                .thenReturn(Optional.of(existing));

                assertThrows(ResourceAlreadyExistsException.class,
                                () -> paymentService.createPaymentIntent(dto));
        }

        @Test
        @DisplayName("Should delete previous pending ticket before creating a new one")
        void shouldDeletePreviousPendingTicketBeforeCreatingNewOne() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), freeEvent.getId());
                PaymentIntentResponseDTO responseDTO = new PaymentIntentResponseDTO(
                                null,
                                null,
                                BigDecimal.ZERO,
                                "eur",
                                200L,
                                "http://localhost:8080/uploads/qr/test.png",
                                freeTicket.getVerificationCode());

                when(eventRepository.findById(freeEvent.getId())).thenReturn(Optional.of(freeEvent));
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(user.getId(), freeEvent.getId()))
                                .thenReturn(Optional.of(pendingTicket));
                when(ticketMapper.toEntity(user, freeEvent, null, PaymentStatus.FREE)).thenReturn(freeTicket);
                when(ticketRepository.save(any(Ticket.class))).thenReturn(freeTicket);
                when(qrService.generateTicketQr(eq(freeTicket.getId()), anyString()))
                                .thenReturn("http://localhost:8080/uploads/qr/test.png");
                when(paymentMapper.toFreeResponse(freeTicket, BigDecimal.ZERO)).thenReturn(responseDTO);

                PaymentIntentResponseDTO result = paymentService.createPaymentIntent(dto);

                assertNotNull(result);
                verify(ticketRepository).delete(pendingTicket);
                verify(ticketRepository).flush();
        }

        @Test
        @DisplayName("Should throw when event does not exist")
        void shouldThrowWhenEventDoesNotExist() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), 999L);

                when(eventRepository.findById(999L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> paymentService.createPaymentIntent(dto));
        }

        @Test
        @DisplayName("Should throw when user does not exist")
        void shouldThrowWhenUserDoesNotExist() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(999L, freeEvent.getId());

                when(eventRepository.findById(freeEvent.getId())).thenReturn(Optional.of(freeEvent));
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> paymentService.createPaymentIntent(dto));
        }

        @Test
        @DisplayName("Should activate pending ticket successfully")
        void shouldActivatePendingTicketSuccessfully() {
                Ticket ticket = new Ticket();
                ticket.setId(50L);
                ticket.setPaymentStatus(PaymentStatus.PENDING);
                ticket.setVerificationCode("old-code");

                when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(qrService.generateTicketQr(50L, ticket.getVerificationCode()))
                                .thenReturn("http://localhost:8080/uploads/qr/generated.png");

                paymentService.activateTicket(ticket);

                assertEquals("http://localhost:8080/uploads/qr/generated.png", ticket.getQrUrl());
                verify(ticketMapper).completeTicketData(PaymentStatus.COMPLETED, ticket);
                verify(ticketRepository, org.mockito.Mockito.times(2)).save(ticket);
        }

        @Test
        @DisplayName("Should not reactivate completed ticket")
        void shouldNotReactivateCompletedTicket() {
                Ticket ticket = new Ticket();
                ticket.setId(51L);
                ticket.setPaymentStatus(PaymentStatus.COMPLETED);

                paymentService.activateTicket(ticket);

                verify(ticketMapper, org.mockito.Mockito.never()).completeTicketData(any(), any());
                verify(ticketRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("Should reject when free ticket already exists")
        void shouldRejectWhenFreeTicketAlreadyExists() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), freeEvent.getId());

                Ticket existing = new Ticket();
                existing.setPaymentStatus(PaymentStatus.FREE);

                when(eventRepository.findById(freeEvent.getId())).thenReturn(Optional.of(freeEvent));
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(user.getId(), freeEvent.getId()))
                                .thenReturn(Optional.of(existing));

                assertThrows(ResourceAlreadyExistsException.class,
                                () -> paymentService.createPaymentIntent(dto));
        }

        @Test
        @DisplayName("Should delete failed ticket before creating a new one")
        void shouldDeleteFailedTicketBeforeCreatingNewOne() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), freeEvent.getId());

                Ticket failedTicket = new Ticket();
                failedTicket.setPaymentStatus(PaymentStatus.FAILED);

                PaymentIntentResponseDTO responseDTO = new PaymentIntentResponseDTO(
                                null,
                                null,
                                BigDecimal.ZERO,
                                "eur",
                                200L,
                                "http://localhost:8080/uploads/qr/test.png",
                                freeTicket.getVerificationCode());

                when(eventRepository.findById(freeEvent.getId())).thenReturn(Optional.of(freeEvent));
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(user.getId(), freeEvent.getId()))
                                .thenReturn(Optional.of(failedTicket));
                when(ticketMapper.toEntity(user, freeEvent, null, PaymentStatus.FREE)).thenReturn(freeTicket);
                when(ticketRepository.save(any(Ticket.class))).thenReturn(freeTicket);
                when(qrService.generateTicketQr(eq(freeTicket.getId()), anyString()))
                                .thenReturn("http://localhost:8080/uploads/qr/test.png");
                when(paymentMapper.toFreeResponse(freeTicket, BigDecimal.ZERO)).thenReturn(responseDTO);

                paymentService.createPaymentIntent(dto);

                verify(ticketRepository).delete(failedTicket);
                verify(ticketRepository).flush();
        }

        @Test
        @DisplayName("Should not generate QR again for completed ticket")
        void shouldNotGenerateQrAgainForCompletedTicket() {
                Ticket ticket = new Ticket();
                ticket.setId(51L);
                ticket.setPaymentStatus(PaymentStatus.COMPLETED);

                paymentService.activateTicket(ticket);

                verify(ticketMapper, org.mockito.Mockito.never()).completeTicketData(any(), any());
                verify(ticketRepository, org.mockito.Mockito.never()).save(any());
                verify(qrService, org.mockito.Mockito.never()).generateTicketQr(any(), anyString());
        }

        @Test
        @DisplayName("Should reject when paid ticket already exists as free")
        void shouldRejectWhenPaidTicketAlreadyExistsAsFree() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), paidEvent.getId());

                Ticket existing = new Ticket();
                existing.setPaymentStatus(PaymentStatus.FREE);

                when(eventRepository.findById(paidEvent.getId())).thenReturn(Optional.of(paidEvent));
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(user.getId(), paidEvent.getId()))
                                .thenReturn(Optional.of(existing));

                assertThrows(ResourceAlreadyExistsException.class,
                                () -> paymentService.createPaymentIntent(dto));
        }

        @Test
        @DisplayName("Should delete pending ticket before creating a paid intent")
        void shouldDeletePendingTicketBeforeCreatingPaidIntent() {
                PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(user.getId(), paidEvent.getId());

                when(eventRepository.findById(paidEvent.getId())).thenReturn(Optional.of(paidEvent));
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                when(ticketRepository.findByUserIdAndEventId(user.getId(), paidEvent.getId()))
                                .thenReturn(Optional.of(pendingTicket));

                assertThrows(RuntimeException.class, () -> paymentService.createPaymentIntent(dto));

                verify(ticketRepository).delete(pendingTicket);
                verify(ticketRepository).flush();
        }

        @Test
        @DisplayName("Should not generate QR for completed ticket")
        void shouldNotGenerateQrForCompletedTicket() {
                Ticket ticket = new Ticket();
                ticket.setId(99L);
                ticket.setPaymentStatus(PaymentStatus.COMPLETED);

                paymentService.activateTicket(ticket);

                verify(qrService, org.mockito.Mockito.never()).generateTicketQr(any(), anyString());
        }

}
