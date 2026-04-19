package com.code.crafters.dto.response;

import java.time.LocalDateTime;

import com.code.crafters.entity.enums.PaymentStatus;

public record TicketResponseDTO(
                Long id,
                LocalDateTime createdAt,
                Long userId,
                String userName,
                String userProfileImage,
                Long eventId,
                String eventTitle,
                PaymentStatus paymentStatus,
                String paymentIntentId,
                String qrUrl,
                String verificationCode) {

}
