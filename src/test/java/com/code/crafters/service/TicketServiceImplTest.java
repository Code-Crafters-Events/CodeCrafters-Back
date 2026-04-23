package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.PageMapper;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketServiceImpl Unit Tests")
@SuppressWarnings({ "null", "unchecked" })
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private PageMapper pageMapper;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User testUser;
    private Event testEvent;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Juan");
        testUser.setEmail("juan@example.com");
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Masterclass Java");
        testEvent.setDescription("Una masterclass completa");
        testEvent.setType(EventType.MASTERCLASS);
        testEvent.setDate(LocalDate.now().plusDays(10));
        testEvent.setTime(LocalTime.of(14, 0));
        testEvent.setMaxAttendees(50);
        testEvent.setCategory(EventCategory.PRESENCIAL);
        testEvent.setPrice(BigDecimal.valueOf(29.99));
        testEvent.setAuthor(testUser);
        testEvent.setTickets(List.of());
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setUser(testUser);
        testTicket.setEvent(testEvent);
        testTicket.setPaymentStatus(PaymentStatus.PENDING);
        testTicket.setVerificationCode(UUID.randomUUID().toString());
        testTicket.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should register user to event successfully")
    void testRegisterToEventSuccess() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.existsByUserIdAndEventId(1L, 1L)).thenReturn(false);
        when(ticketMapper.toEntity(eq(testUser), eq(testEvent), any(), eq(PaymentStatus.PENDING)))
                .thenReturn(testTicket);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        Ticket result = ticketService.registerToEvent(1L, 1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUser().getId());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void testRegisterToEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ticketService.registerToEvent(1L, 999L));
    }

    @Test
    @DisplayName("Should throw exception when user already registered")
    void testRegisterToEventAlreadyRegistered() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.existsByUserIdAndEventId(1L, 1L)).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> ticketService.registerToEvent(1L, 1L));
    }

    @Test
    @DisplayName("Should throw exception when event is full")
    void testRegisterToEventFull() {
        testEvent.setMaxAttendees(1);
        testEvent.setTickets(List.of(testTicket));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        assertThrows(ForbiddenOperationException.class, () -> ticketService.registerToEvent(1L, 1L));
        verifyNoInteractions(ticketRepository);
    }

    @Test
    @DisplayName("Should unregister from event successfully")
    void testUnregisterFromEventSuccess() {
        when(ticketRepository.findByUserIdAndEventId(1L, 1L)).thenReturn(Optional.of(testTicket));
        assertDoesNotThrow(() -> ticketService.unregisterFromEvent(1L, 1L));
        verify(ticketRepository, times(1)).delete(any(Ticket.class));
    }

    @Test
    @DisplayName("Should refund payment when unregistering from paid event")
    void testUnregisterFromEventWithRefund() {
        testTicket.setPaymentStatus(PaymentStatus.COMPLETED);
        testTicket.setPaymentIntentId("pi_test123");
        when(ticketRepository.findByUserIdAndEventId(1L, 1L)).thenReturn(Optional.of(testTicket));
        assertDoesNotThrow(() -> ticketService.unregisterFromEvent(1L, 1L));
        verify(paymentService, times(1)).refundPayment("pi_test123");
        verify(ticketRepository, times(1)).delete(any(Ticket.class));
    }

    @Test
    @DisplayName("Should throw exception when unregistering used ticket")
    void testUnregisterFromEventAlreadyUsed() {
        testTicket.setUsedAt(LocalDateTime.now()); // Ticket ya usado
        when(ticketRepository.findByUserIdAndEventId(1L, 1L)).thenReturn(Optional.of(testTicket));
        assertThrows(ForbiddenOperationException.class, () -> ticketService.unregisterFromEvent(1L, 1L));
    }

    @Test
    @DisplayName("Should verify valid ticket successfully")
    void testVerifyTicketSuccess() {
        testTicket.setPaymentStatus(PaymentStatus.COMPLETED);
        String code = testTicket.getVerificationCode();
        when(ticketRepository.findByVerificationCode(code)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketMapper.toVerificationResponse(any(), eq(true), anyString()))
                .thenReturn(createVerificationResponse(true));
        TicketVerificationResponseDTO result = ticketService.verifyTicket(code);
        assertTrue(result.valid());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should return not found response for invalid code")
    void testVerifyTicketNotFound() {
        when(ticketRepository.findByVerificationCode("invalid")).thenReturn(Optional.empty());
        when(ticketMapper.toNotFoundResponse(anyString())).thenReturn(createNotFoundResponse());
        TicketVerificationResponseDTO result = ticketService.verifyTicket("invalid");
        assertFalse(result.valid());
        assertEquals("Ticket no encontrado", result.message());
    }

    @Test
    @DisplayName("Should get tickets by user successfully")
    void testGetTicketsByUserSuccess() {
        Page<Ticket> page = new PageImpl<>(List.of(testTicket));
        PageResponseDTO<TicketResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(createTicketResponse()), 0, 10, 1, 1, true);
        when(ticketRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(pageMapper.toTicketPageResponse(any(Page.class))).thenReturn(pageResponse);
        PageResponseDTO<TicketResponseDTO> result = ticketService.getTicketsByUser(1L, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should get tickets by event successfully")
    void testGetTicketsByEventSuccess() {
        Page<Ticket> page = new PageImpl<>(List.of(testTicket));
        PageResponseDTO<TicketResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(createTicketResponse()), 0, 10, 1, 1, true);

        when(ticketRepository.findByEventId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(pageMapper.toTicketPageResponse(any(Page.class))).thenReturn(pageResponse);
        PageResponseDTO<TicketResponseDTO> result = ticketService.getTicketsByEvent(1L, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should get total ticket count")
    void testGetTicketCount() {
        when(ticketRepository.count()).thenReturn(100L);
        Long result = ticketService.getTicketCount();
        assertEquals(100L, result);
    }

    private TicketResponseDTO createTicketResponse() {
        return new TicketResponseDTO(
                1L, LocalDateTime.now(), 1L, "Juan", null, 1L,
                "Masterclass Java", PaymentStatus.PENDING, null, null,
                testTicket.getVerificationCode());
    }

    private TicketVerificationResponseDTO createVerificationResponse(boolean valid) {
        return new TicketVerificationResponseDTO(
                valid, valid ? "Ticket válido ✓" : "Pago no confirmado",
                1L, "Masterclass Java", "Juan", LocalDateTime.now(),
                null, PaymentStatus.COMPLETED);
    }

    private TicketVerificationResponseDTO createNotFoundResponse() {
        return new TicketVerificationResponseDTO(
                false, "Ticket no encontrado", null, null, null, null, null, null);
    }
}