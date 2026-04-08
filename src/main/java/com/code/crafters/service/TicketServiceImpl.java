package com.code.crafters.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;

    @Override
    public Ticket registerToEvent(Long userId, Long eventId) {
        if (ticketRepository.existsByUserIdAndEventId(userId, eventId))
            throw new ResourceAlreadyExistsException("Ya estás apuntado a este evento");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventId));
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setCreatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Override
public void unregisterFromEvent(Long userId, Long eventId) {
    Ticket ticket = ticketRepository.findByUserIdAndEventId(userId, eventId)
            .orElseThrow(() -> new ResourceNotFoundException("No estás apuntado a este evento"));
    ticketRepository.delete(ticket);
}

    @Override
public PageResponseDTO<TicketResponseDTO> getTicketsByUser(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Ticket> result = ticketRepository.findByUserId(userId, pageable);
    List<TicketResponseDTO> content = result.getContent()
            .stream()
            .map(ticketMapper::toResponse)
            .toList();
    return new PageResponseDTO<>(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isLast()
    );
}

   @Override
public PageResponseDTO<TicketResponseDTO> getTicketsByEvent(Long eventId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Ticket> result = ticketRepository.findByEventId(eventId, pageable);
    List<TicketResponseDTO> content = result.getContent()
            .stream()
            .map(ticketMapper::toResponse)
            .toList();
    return new PageResponseDTO<>(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isLast()
    );
}


}
