package com.code.crafters.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;

@SuppressWarnings("null")
@DisplayName("EmailServiceImpl Tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
    }

    @Test
    void shouldSendCancellationEmail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(templateEngine.process(eq("email-cancellation"), any(Context.class)))
                .thenReturn("<html>content</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendCancellationEmail(
                "juan@example.com", "Evento", "Juan", BigDecimal.valueOf(29.99));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldNotThrowWhenCancellationEmailFails() {
        when(templateEngine.process(eq("email-cancellation"), any(Context.class)))
                .thenThrow(new RuntimeException("template error"));

        emailService.sendCancellationEmail(
                "juan@example.com", "Evento", "Juan", BigDecimal.valueOf(29.99));
    }

    @Test
    void shouldSendBulkCancellationEmailOnlyForCompletedOrFreeTickets() {
        Ticket completed = buildTicket(PaymentStatus.COMPLETED);
        Ticket free = buildTicket(PaymentStatus.FREE);
        Ticket pending = buildTicket(PaymentStatus.PENDING);
        Ticket failed = buildTicket(PaymentStatus.FAILED);

        EmailServiceImpl spyService = spy(emailService);

        spyService.sendBulkCancellationEmail(
                List.of(completed, free, pending, failed), "Evento", BigDecimal.TEN);

        verify(spyService, times(2))
                .sendCancellationEmail(eq("user@example.com"), eq("Evento"), eq("Juan"), eq(BigDecimal.TEN));
    }

    private Ticket buildTicket(PaymentStatus status) {
        User user = new User();
        user.setEmail("user@example.com");
        user.setName("Juan");

        Event event = new Event();
        event.setTitle("Evento");

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setPaymentStatus(status);
        return ticket;
    }
}
