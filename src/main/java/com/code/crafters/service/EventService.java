package com.code.crafters.service;

import com.code.crafters.dto.request.EventFilterDTO;
import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.entity.Event;

public interface EventService {
    PageResponseDTO<EventResponseDTO> getAllEvents(int page, int size);

    PageResponseDTO<EventResponseDTO> getEventsByUser(Long userId, int page, int size);

    PageResponseDTO<EventResponseDTO> searchEvents(EventFilterDTO filter, int page, int size);

    Event createEvent(EventRequestDTO dto, Long authorId);

    Event getEventById(Long id);

    Event updateEvent(Long id, EventRequestDTO dto, Long authorId);

    void deleteEvent(Long id, Long authorId);
}
