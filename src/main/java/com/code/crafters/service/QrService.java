package com.code.crafters.service;

public interface QrService {
    String generateTicketQr(Long ticketId, String verificationCode);
}
