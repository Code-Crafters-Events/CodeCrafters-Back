package com.code.crafters.service;

import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.entity.Ticket;

public interface TicketService {
    Ticket registerToEvent(Long userId, Long eventId);

    void unregisterFromEvent(Long userId, Long eventId);

    PageResponseDTO<TicketResponseDTO> getTicketsByUser(Long userId, int page, int size);

    PageResponseDTO<TicketResponseDTO> getTicketsByEvent(Long eventId, int page, int size);

    TicketVerificationResponseDTO verifyTicket(String verificationCode);

    Long getTicketCount();

}
