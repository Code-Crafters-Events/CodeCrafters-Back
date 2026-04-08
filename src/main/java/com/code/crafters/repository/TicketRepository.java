package com.code.crafters.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.code.crafters.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByUserId(Long userId, Pageable pageable);

    Page<Ticket> findByEventId(Long eventId, Pageable pageable);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    Optional<Ticket> findByUserIdAndEventId(Long userId, Long eventId);
}
