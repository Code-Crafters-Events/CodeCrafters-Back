package com.code.crafters.dto.response;

import java.math.BigDecimal;

public record PaymentIntentResponseDTO(
                String clientSecret,
                String paymentIntentId,
                BigDecimal amount,
                String currency,
                Long ticketId,
                String qrUrl,
                String verificationCode) {

}
