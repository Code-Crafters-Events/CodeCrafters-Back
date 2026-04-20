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
    public Ticket registerToEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventId));
        if (event.getTickets() != null && event.getTickets().size() >= event.getMaxAttendees()) {
            throw new ForbiddenOperationException("El evento ha alcanzado el máximo de asistentes permitidos.");
        }
        if (ticketRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ResourceAlreadyExistsException("Ya estás apuntado a este evento");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        return ticketRepository.save(ticketMapper.toEntity(user, event, null, PaymentStatus.PENDING));
    }

    @Override
    @Transactional
    public void unregisterFromEvent(Long userId, Long eventId) {
        Ticket ticket = ticketRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un ticket para este evento"));

        if (ticket.getUsedAt() != null) {
            throw new ForbiddenOperationException("No se puede cancelar un ticket que ya ha sido escaneado/usado");
        }

        if (ticket.getPaymentStatus() == PaymentStatus.COMPLETED) {
            if (ticket.getPaymentIntentId() != null) {
                paymentService.refundPayment(ticket.getPaymentIntentId());
            }
        }

        ticketRepository.delete(ticket);
        log.info("Ticket eliminado y dinero devuelto (si procedía) para usuario {} en evento {}", userId, eventId);
    }

    @Override
    public PageResponseDTO<TicketResponseDTO> getTicketsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return pageMapper.toTicketPageResponse(ticketRepository.findByUserId(userId, pageable));
    }

    @Override
    public PageResponseDTO<TicketResponseDTO> getTicketsByEvent(Long eventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return pageMapper.toTicketPageResponse(ticketRepository.findByEventId(eventId, pageable));
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

    @Override
    public Long getTicketCount() {
        return ticketRepository.count();
    }
}