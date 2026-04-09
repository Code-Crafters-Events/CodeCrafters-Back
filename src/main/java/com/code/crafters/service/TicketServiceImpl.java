package com.code.crafters.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.PageMapper;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TicketServiceImpl implements TicketService, PageMapper {
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

        return ticketRepository.save(ticketMapper.toEntity(user, event, null, null));
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
        return toPageResponse(ticketRepository.findByUserId(userId, pageable), ticketMapper::toResponse);
    }

    @Override
    public PageResponseDTO<TicketResponseDTO> getTicketsByEvent(Long eventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toPageResponse(ticketRepository.findByEventId(eventId, pageable), ticketMapper::toResponse);
    }

    @Override
    public TicketVerificationResponseDTO verifyTicket(String verificationCode) {
        Ticket ticket = ticketRepository.findByVerificationCode(verificationCode)
                .orElse(null);
        if (ticket == null)
            return ticketMapper.toNotFoundResponse("Ticket no encontrado");
        if (ticket.getPaymentStatus() != PaymentStatus.COMPLETED)
            return ticketMapper.toVerificationResponse(ticket, false, "El pago no está confirmado");
        if (ticket.getUsedAt() != null)
            return ticketMapper.toVerificationResponse(ticket, false,
                    "Ticket ya utilizado el " + ticket.getUsedAt());
        ticket.setUsedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return ticketMapper.toVerificationResponse(ticket, true, "Ticket válido ✓");
    }
}