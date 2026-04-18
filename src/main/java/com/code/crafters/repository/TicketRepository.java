package com.code.crafters.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.enums.PaymentStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByUserId(Long userId, Pageable pageable);

    Page<Ticket> findByEventId(Long eventId, Pageable pageable);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    Optional<Ticket> findByUserIdAndEventId(Long userId, Long eventId);

    Optional<Ticket> findByPaymentIntentId(String paymentIntentId);

    Optional<Ticket> findByVerificationCode(String verificationCode);

    @Modifying
    @Query("DELETE FROM Ticket t WHERE t.paymentStatus = :status AND t.createdAt < :cutoff")
    long deleteByPaymentStatusAndCreatedAtBefore(
            @Param("status") PaymentStatus status,
            @Param("cutoff") LocalDateTime cutoff);
}
