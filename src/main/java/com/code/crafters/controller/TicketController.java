package com.code.crafters.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> register(@RequestParam Long userId,
            @RequestParam Long eventId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketMapper.toResponse(ticketService.registerToEvent(userId, eventId)));
    }

    @DeleteMapping
    public ResponseEntity<Void> unregister(@RequestParam Long userId, @RequestParam Long eventId) {
        ticketService.unregisterFromEvent(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponseDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity
                .ok(ticketService.getTicketsByUser(userId).stream().map(ticketMapper::toResponse).toList());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketResponseDTO>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity
                .ok(ticketService.getTicketsByEvent(eventId).stream().map(ticketMapper::toResponse).toList());
    }
}
