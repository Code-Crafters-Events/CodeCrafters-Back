package com.code.crafters.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.code.crafters.dto.request.EventFilterDTO;
import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Location;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.EventMapper;
import com.code.crafters.mapper.PageMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.LocationRepository;
import com.code.crafters.repository.UserRepository;
import com.code.crafters.specification.EventSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EventServiceImpl implements EventService, PageMapper {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;

    @Override
    public Event createEvent(EventRequestDTO dto, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + authorId));
        Event event = eventMapper.toEntity(dto);
        event.setAuthor(author);
        if (dto.locationId() != null) {
            Location location = locationRepository.findById(dto.locationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada: " + dto.locationId()));
            event.setLocation(location);
        }
        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
    }

    @Override
    public PageResponseDTO<EventResponseDTO> getAllEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
        return toPageResponse(eventRepository.findAll(pageable), eventMapper::toResponse);
    }

    @Override
    public PageResponseDTO<EventResponseDTO> getEventsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
        return toPageResponse(eventRepository.findByAuthorId(userId, pageable), eventMapper::toResponse);
    }

    @Override
    public Event updateEvent(Long id, EventRequestDTO dto, Long authorId) {
        Event event = getEventById(id);
        if (!event.getAuthor().getId().equals(authorId))
            throw new ForbiddenOperationException("No tienes permiso para editar este evento");
        eventMapper.updateEntity(dto, event);
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id, Long authorId) {
        Event event = getEventById(id);
        if (!event.getAuthor().getId().equals(authorId))
            throw new ForbiddenOperationException("No tienes permiso para eliminar este evento");
        eventRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<EventResponseDTO> searchEvents(EventFilterDTO filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
        Specification<Event> spec = EventSpecification.withFilters(filter);
        return toPageResponse(eventRepository.findAll(spec, pageable), eventMapper::toResponse);
    }
}
