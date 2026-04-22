package com.code.crafters.service;

import com.code.crafters.dto.request.PaymentIntentRequestDTO;
import com.code.crafters.dto.response.PaymentIntentResponseDTO;
import com.code.crafters.entity.Ticket;

public interface PaymentService {
    PaymentIntentResponseDTO createPaymentIntent(PaymentIntentRequestDTO dto);

    void handleWebhookEvent(String payload, String sigHeader);

    void cleanupAbandonedPendingTickets();

    void refundPayment(String paymentIntentId);

    void activateTicket(Ticket ticket);
}
