package com.code.crafters.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.exception.GlobalExceptionHandler;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.security.JwtService;
import com.code.crafters.service.TicketService;

@SuppressWarnings("null")
@DisplayName("TicketController Tests")
class TicketControllerTest {

    private MockMvc mockMvc;
    private TicketService ticketService;
    private TicketMapper ticketMapper;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ticketService = org.mockito.Mockito.mock(TicketService.class);
        ticketMapper = org.mockito.Mockito.mock(TicketMapper.class);
        jwtService = org.mockito.Mockito.mock(JwtService.class);

        TicketController controller = new TicketController(ticketService, ticketMapper, jwtService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterTicketSuccessfully() throws Exception {
        Ticket ticket = new Ticket();
        TicketResponseDTO response = new TicketResponseDTO(
                1L,
                LocalDateTime.now(),
                5L,
                "Juan",
                null,
                10L,
                "Workshop",
                PaymentStatus.PENDING,
                null,
                null,
                "code-123");

        when(jwtService.extractUserId(anyString())).thenReturn(5L);
        when(ticketService.registerToEvent(5L, 10L)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", "Bearer token")
                        .param("eventId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.eventId").value(10));

        verify(ticketService).registerToEvent(5L, 10L);
        verify(ticketMapper).toResponse(ticket);
    }

    @Test
    void shouldUnregisterTicketSuccessfully() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(5L);

        mockMvc.perform(delete("/api/v1/tickets")
                        .header("Authorization", "Bearer token")
                        .param("eventId", "10"))
                .andExpect(status().isNoContent());

        verify(ticketService).unregisterFromEvent(5L, 10L);
    }

    @Test
    void shouldGetTicketsByUser() throws Exception {
        TicketResponseDTO ticket = new TicketResponseDTO(
                1L, LocalDateTime.now(), 5L, "Juan", null, 10L,
                "Workshop", PaymentStatus.PENDING, null, null, "code");
        PageResponseDTO<TicketResponseDTO> page = new PageResponseDTO<>(
                List.of(ticket), 0, 10, 1, 1, true);

        when(ticketService.getTicketsByUser(5L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets/user/{userId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(5));
    }

    @Test
    void shouldGetTicketsByUserWithCustomPaging() throws Exception {
        TicketResponseDTO ticket = new TicketResponseDTO(
                1L, LocalDateTime.now(), 5L, "Juan", null, 10L,
                "Workshop", PaymentStatus.PENDING, null, null, "code");
        PageResponseDTO<TicketResponseDTO> page = new PageResponseDTO<>(
                List.of(ticket), 1, 5, 1, 1, true);

        when(ticketService.getTicketsByUser(5L, 1, 5)).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets/user/{userId}", 5L)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void shouldGetTicketsByEvent() throws Exception {
        TicketResponseDTO ticket = new TicketResponseDTO(
                1L, LocalDateTime.now(), 5L, "Juan", null, 10L,
                "Workshop", PaymentStatus.PENDING, null, null, "code");
        PageResponseDTO<TicketResponseDTO> page = new PageResponseDTO<>(
                List.of(ticket), 0, 10, 1, 1, true);

        when(ticketService.getTicketsByEvent(10L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets/event/{eventId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventId").value(10));
    }

    @Test
    void shouldGetTicketsByEventWithCustomPaging() throws Exception {
        TicketResponseDTO ticket = new TicketResponseDTO(
                1L, LocalDateTime.now(), 5L, "Juan", null, 10L,
                "Workshop", PaymentStatus.PENDING, null, null, "code");
        PageResponseDTO<TicketResponseDTO> page = new PageResponseDTO<>(
                List.of(ticket), 2, 3, 1, 1, true);

        when(ticketService.getTicketsByEvent(10L, 2, 3)).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets/event/{eventId}", 10L)
                        .param("page", "2")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(3));
    }

    @Test
    void shouldVerifyTicket() throws Exception {
        TicketVerificationResponseDTO response = new TicketVerificationResponseDTO(
                true,
                "Ticket valido",
                1L,
                "Workshop",
                "Juan",
                LocalDateTime.now(),
                null,
                PaymentStatus.COMPLETED);

        when(ticketService.verifyTicket("abc123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/tickets/verify/{verificationCode}", "abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.ticketId").value(1));
    }

    @Test
    void shouldReturnVerificationResponsePayload() throws Exception {
        TicketVerificationResponseDTO response = new TicketVerificationResponseDTO(
                false,
                "Pago no confirmado",
                1L,
                "Workshop",
                "Juan",
                LocalDateTime.now(),
                null,
                PaymentStatus.PENDING);

        when(ticketService.verifyTicket("pending-code")).thenReturn(response);

        mockMvc.perform(get("/api/v1/tickets/verify/{verificationCode}", "pending-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pago no confirmado"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"));
    }

    @Test
    void shouldGetTicketCount() throws Exception {
        when(ticketService.getTicketCount()).thenReturn(12L);

        mockMvc.perform(get("/api/v1/tickets/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(12));
    }

    @Test
    void shouldReturnForbiddenWhenAuthorizationHeaderIsInvalidOnRegister() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", "invalid-token")
                        .param("eventId", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenAuthorizationHeaderIsInvalidOnDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/tickets")
                        .header("Authorization", "invalid-token")
                        .param("eventId", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnInternalServerErrorWhenAuthorizationHeaderIsMissingOnRegister() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .param("eventId", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnInternalServerErrorWhenAuthorizationHeaderIsMissingOnUnregister() throws Exception {
        mockMvc.perform(delete("/api/v1/tickets")
                        .param("eventId", "10"))
                .andExpect(status().isForbidden());
    }
}
