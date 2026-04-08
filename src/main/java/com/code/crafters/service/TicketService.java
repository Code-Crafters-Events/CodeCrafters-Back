package com.code.crafters.service;

import java.util.List;

import com.code.crafters.entity.Ticket;

public interface TicketService {
    Ticket registerToEvent(Long userId, Long eventId);

    void unregisterFromEvent(Long userId, Long eventId);

    List<Ticket> getTicketsByUser(Long userId);

    List<Ticket> getTicketsByEvent(Long eventId);
}
