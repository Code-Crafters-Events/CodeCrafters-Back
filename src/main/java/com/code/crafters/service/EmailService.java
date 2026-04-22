package com.code.crafters.service;

import java.util.List;

import com.code.crafters.entity.Ticket;

public interface EmailService {
    void sendCancellationEmail(String to, String eventTitle, String userName, java.math.BigDecimal price);

    void sendBulkCancellationEmail(List<Ticket> tickets, String eventTitle, java.math.BigDecimal price);
}
