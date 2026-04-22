package com.code.crafters.service;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.enums.PaymentStatus;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendCancellationEmail(String to, String eventTitle, String userName, BigDecimal price) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("eventTitle", eventTitle);
            context.setVariable("price", price);

            String htmlContent = templateEngine.process("email-cancellation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Crafters Events");
            helper.setTo(to);
            helper.setSubject("⚠️ Cancelación de Evento: " + eventTitle);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error enviando email a " + to + ": " + e.getMessage());
        }
    }

    @Override
    public void sendBulkCancellationEmail(List<Ticket> tickets, String eventTitle, BigDecimal price) {
        for (Ticket ticket : tickets) {
            if (ticket.getPaymentStatus() == PaymentStatus.COMPLETED ||
                    ticket.getPaymentStatus() == PaymentStatus.FREE) {
                this.sendCancellationEmail(
                        ticket.getUser().getEmail(),
                        eventTitle,
                        ticket.getUser().getName(),
                        price);
            }
        }
    }
}