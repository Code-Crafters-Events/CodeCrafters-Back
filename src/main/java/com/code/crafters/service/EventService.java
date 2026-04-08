package com.code.crafters.service;

import java.util.List;

import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.entity.Event;

public interface EventService {
    Event createEvent(EventRequestDTO dto, Long authorId);

    Event getEventById(Long id);

    List<Event> getAllEvents();

    List<Event> getEventsByUser(Long userId);

    Event updateEvent(Long id, EventRequestDTO dto, Long authorId);

    void deleteEvent(Long id, Long authorId);
}
