package com.code.crafters.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentIntentRequestDTO Validation Tests")
class PaymentIntentRequestDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(1L, 2L);

        Set<ConstraintViolation<PaymentIntentRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserIdAndEventIdAreNull() {
        PaymentIntentRequestDTO dto = new PaymentIntentRequestDTO(null, null);

        Set<ConstraintViolation<PaymentIntentRequestDTO>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
    }
}
