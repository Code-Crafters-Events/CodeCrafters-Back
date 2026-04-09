package com.code.crafters.service;

import com.code.crafters.dto.request.PaymentIntentRequestDTO;
import com.code.crafters.dto.response.PaymentIntentResponseDTO;

public interface PaymentService {
    PaymentIntentResponseDTO createPaymentIntent(PaymentIntentRequestDTO dto);

    void handleWebhookEvent(String payload, String sigHeader);
}
