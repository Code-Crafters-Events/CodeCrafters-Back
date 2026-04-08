package com.code.crafters.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.mapper.EventMapper;
import com.code.crafters.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping
    public ResponseEntity<EventResponseDTO> create(@Valid @RequestBody EventRequestDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventMapper.toResponse(eventService.createEvent(dto, userId)));
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAll() {
        return ResponseEntity.ok(eventService.getAllEvents().stream().map(eventMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventMapper.toResponse(eventService.getEventById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventResponseDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getEventsByUser(userId).stream().map(eventMapper::toResponse).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody EventRequestDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity.ok(eventMapper.toResponse(eventService.updateEvent(id, dto, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        eventService.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

}
