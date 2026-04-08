package com.code.crafters.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Location;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.EventMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.LocationRepository;
import com.code.crafters.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
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
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> getEventsByUser(Long userId) {
        return eventRepository.findByAuthorId(userId);
    }

    @Override
    public Event updateEvent(Long id, EventRequestDTO dto, Long authorId) {
        Event event = getEventById(id);
        if (!event.getAuthor().getId().equals(authorId))
            throw new ForbiddenOperationException("No tienes permiso para editar este evento");
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setType(dto.type());
        event.setDate(dto.date());
        event.setTime(dto.time());
        event.setMaxAttendees(dto.maxAttendees());
        event.setCategory(dto.category());
        event.setPrice(dto.price());
        event.setImageUrl(dto.imageUrl());
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id, Long authorId) {
        Event event = getEventById(id);
        if (!event.getAuthor().getId().equals(authorId))
            throw new ForbiddenOperationException("No tienes permiso para eliminar este evento");
        eventRepository.deleteById(id);
    }
}
