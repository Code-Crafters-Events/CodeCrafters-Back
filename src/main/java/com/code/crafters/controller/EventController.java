package com.code.crafters.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.code.crafters.dto.request.EventFilterDTO;
import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.service.EventService;
import com.code.crafters.security.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<EventResponseDTO> create(
            @Valid @RequestBody EventRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(dto, userId));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(eventService.getAllEvents(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(eventService.getEventsByUser(userId, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        Long currentUserId = extractId(authHeader);
        EventResponseDTO updated = eventService.updateEvent(id, dto, currentUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractId(authHeader);
        eventService.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> search(
            @Valid EventFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(eventService.searchEvents(filter, page, size));
    }

    private Long extractId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}