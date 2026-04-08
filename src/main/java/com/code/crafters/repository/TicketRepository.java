package com.code.crafters.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.code.crafters.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);

    List<Ticket> findByEventId(Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
