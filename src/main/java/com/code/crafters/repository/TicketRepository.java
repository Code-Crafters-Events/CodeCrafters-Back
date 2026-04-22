package com.code.crafters.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
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

        List<Ticket> findByEventId(Long eventId);

        @Modifying
        @Query("DELETE FROM Ticket t WHERE t.paymentStatus = :status AND t.createdAt < :cutoff")
        long deleteByPaymentStatusAndCreatedAtBefore(
                        @Param("status") PaymentStatus status,
                        @Param("cutoff") LocalDateTime cutoff);

        @Modifying
        @Query("DELETE FROM Ticket t WHERE t.event.date < :eventDate OR (t.event.date = :eventDate AND t.event.time < :eventTime)")
        long deleteAllTicketsForPastEvents(
                        @Param("eventDate") LocalDate eventDate,
                        @Param("eventTime") LocalTime eventTime);

        @Query("SELECT t FROM Ticket t WHERE t.event.date < :eventDate OR (t.event.date = :eventDate AND t.event.time < :eventTime) ORDER BY t.event.date DESC")
        List<Ticket> findAllTicketsForPastEvents(
                        @Param("eventDate") LocalDate eventDate,
                        @Param("eventTime") LocalTime eventTime);

        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.date < :eventDate OR (t.event.date = :eventDate AND t.event.time < :eventTime)")
        long countTicketsForPastEvents(
                        @Param("eventDate") LocalDate eventDate,
                        @Param("eventTime") LocalTime eventTime);

        List<Ticket> findByPaymentStatus(PaymentStatus status);
}