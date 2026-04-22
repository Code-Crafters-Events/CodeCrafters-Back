package com.code.crafters.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.PageMapper;
import com.code.crafters.mapper.TicketMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;
    private final PageMapper pageMapper;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public Ticket registerToEvent(Long userId, Long eventId) {
        Event event = findEventOrThrow(eventId);
        validateEventCapacity(event);
        validateUserNotRegistered(userId, eventId);
        User user = findUserOrThrow(userId);

        return ticketRepository.save(ticketMapper.toEntity(user, event, null, PaymentStatus.PENDING));
    }

    @Override
    @Transactional
    public void unregisterFromEvent(Long userId, Long eventId) {
        Ticket ticket = findTicketOrThrow(userId, eventId);
        validateTicketNotUsed(ticket);

        if (ticket.getPaymentStatus() == PaymentStatus.COMPLETED && ticket.getPaymentIntentId() != null) {
            paymentService.refundPayment(ticket.getPaymentIntentId());
        }

        ticketRepository.delete(ticket);
    }

    @Override
    public TicketVerificationResponseDTO verifyTicket(String code) {
        return ticketRepository.findByVerificationCode(code)
                .map(this::processVerification)
                .orElseGet(() -> ticketMapper.toNotFoundResponse("Ticket no encontrado"));
    }

    @Override
    public PageResponseDTO<TicketResponseDTO> getTicketsByUser(Long userId, int page, int size) {
        return pageMapper.toTicketPageResponse(ticketRepository.findByUserId(userId, createPageable(page, size)));
    }

    @Override
    public PageResponseDTO<TicketResponseDTO> getTicketsByEvent(Long eventId, int page, int size) {
        return pageMapper.toTicketPageResponse(ticketRepository.findByEventId(eventId, createPageable(page, size)));
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by("createdAt").descending());
    }

    private TicketVerificationResponseDTO processVerification(Ticket ticket) {
        if (ticket.getPaymentStatus() != PaymentStatus.COMPLETED)
            return ticketMapper.toVerificationResponse(ticket, false, "Pago no confirmado");

        if (ticket.getUsedAt() != null)
            return ticketMapper.toVerificationResponse(ticket, false, "Ya usado: " + ticket.getUsedAt());

        ticket.setUsedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return ticketMapper.toVerificationResponse(ticket, true, "Ticket válido ✓");
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Ticket findTicketOrThrow(Long uId, Long eId) {
        return ticketRepository.findByUserIdAndEventId(uId, eId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado"));
    }

    private void validateEventCapacity(Event event) {
        if (event.getTickets() != null && event.getTickets().size() >= event.getMaxAttendees()) {
            throw new ForbiddenOperationException("Evento lleno");
        }
    }

    private void validateUserNotRegistered(Long uId, Long eId) {
        if (ticketRepository.existsByUserIdAndEventId(uId, eId)) {
            throw new ResourceAlreadyExistsException("Ya estás apuntado");
        }
    }

    private void validateTicketNotUsed(Ticket ticket) {
        if (ticket.getUsedAt() != null) {
            throw new ForbiddenOperationException("No se puede cancelar un ticket ya usado");
        }
    }

    @Override
    public Long getTicketCount() {
        return ticketRepository.count();
    }
}