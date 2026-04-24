package com.code.crafters.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.security.JwtService;
import com.code.crafters.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Validated
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> register(
            @RequestParam Long eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketMapper.toResponse(ticketService.registerToEvent(userId, eventId)));
    }

    @DeleteMapping
    public ResponseEntity<Void> unregister(
            @RequestParam Long eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractId(authHeader);
        ticketService.unregisterFromEvent(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDTO<TicketResponseDTO>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ticketService.getTicketsByUser(userId, page, size));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<PageResponseDTO<TicketResponseDTO>> getByEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ticketService.getTicketsByEvent(eventId, page, size));
    }

    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<TicketVerificationResponseDTO> verify(
            @PathVariable String verificationCode) {
        return ResponseEntity.ok(ticketService.verifyTicket(verificationCode));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTicketCount() {
        return ResponseEntity.ok(ticketService.getTicketCount());
    }

    private Long extractId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ForbiddenOperationException("Token no válido o ausente");
        }
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
