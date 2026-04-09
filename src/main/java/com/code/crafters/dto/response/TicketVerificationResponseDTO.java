package com.code.crafters.dto.response;

import java.time.LocalDateTime;

import com.code.crafters.entity.enums.PaymentStatus;

public record TicketVerificationResponseDTO(
    boolean valid,
        String message,
        Long ticketId,
        String eventTitle,
        String userName,
        LocalDateTime purchasedAt,
        LocalDateTime usedAt,
        PaymentStatus paymentStatus
) {

}
