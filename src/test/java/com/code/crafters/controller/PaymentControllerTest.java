package com.code.crafters.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.code.crafters.dto.response.PaymentIntentResponseDTO;
import com.code.crafters.exception.GlobalExceptionHandler;
import com.code.crafters.service.PaymentService;

@SuppressWarnings("null")
@DisplayName("PaymentController Tests")
class PaymentControllerTest {

        private MockMvc mockMvc;
        private PaymentService paymentService;

        @BeforeEach
        void setUp() {
                paymentService = org.mockito.Mockito.mock(PaymentService.class);

                PaymentController controller = new PaymentController(paymentService);

                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        void shouldCreatePaymentIntentSuccessfully() throws Exception {
                PaymentIntentResponseDTO response = new PaymentIntentResponseDTO(
                                "secret_123",
                                "pi_123",
                                BigDecimal.valueOf(49.99),
                                "eur",
                                1L,
                                "qr.png",
                                "code-123");

                when(paymentService.createPaymentIntent(any())).thenReturn(response);

                String body = """
                                {
                                  "userId":1,
                                  "eventId":2
                                }
                                """;

                mockMvc.perform(post("/api/v1/payments/create-intent")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.clientSecret").value("secret_123"))
                                .andExpect(jsonPath("$.paymentIntentId").value("pi_123"));
        }

        @Test
        void shouldReturnBadRequestForInvalidCreateIntentPayload() throws Exception {
                String body = """
                                {
                                  "userId":null,
                                  "eventId":null
                                }
                                """;

                mockMvc.perform(post("/api/v1/payments/create-intent")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldProcessWebhookSuccessfully() throws Exception {
                String payload = "{\"id\":\"evt_test\",\"type\":\"payment_intent.succeeded\"}";

                mockMvc.perform(post("/api/v1/payments/webhook")
                                .header("Stripe-Signature", "signature")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(content().string("OK"));

                verify(paymentService).handleWebhookEvent(eq(payload), eq("signature"));
        }

        @Test
        void shouldReturnInternalServerErrorWhenWebhookFails() throws Exception {
                String payload = "{\"id\":\"evt_test\",\"type\":\"payment_intent.succeeded\"}";

                doThrow(new RuntimeException("Webhook error"))
                                .when(paymentService).handleWebhookEvent(eq(payload), eq("signature"));

                mockMvc.perform(post("/api/v1/payments/webhook")
                                .header("Stripe-Signature", "signature")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isInternalServerError());
        }
}
