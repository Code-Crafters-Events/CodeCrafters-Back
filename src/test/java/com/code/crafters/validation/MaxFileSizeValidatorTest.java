package com.code.crafters.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaxFileSizeValidatorTest {

    private MaxFileSizeValidator validator;

    @Mock
    private MaxFileSize annotation;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new MaxFileSizeValidator();
        when(annotation.maxMB()).thenReturn(5L);
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("Debe retornar true cuando el valor es null")
    void shouldReturnTrue_whenValueIsNull() {
        assertTrue(validator.isValid(null, context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Debe retornar true cuando el valor está en blanco")
    void shouldReturnTrue_whenValueIsBlank() {
        assertTrue(validator.isValid("   ", context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Debe retornar true cuando la imagen en base64 puro no supera el límite")
    void shouldReturnTrue_whenBase64WithoutPrefix_isUnderLimit() {
        String smallBase64 = Base64.getEncoder().encodeToString(new byte[100]);

        assertTrue(validator.isValid(smallBase64, context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Debe retornar false cuando la imagen en base64 puro supera el límite")
    void shouldReturnFalse_whenBase64WithoutPrefix_exceedsLimit() {
        byte[] bigData = new byte[6 * 1024 * 1024]; // 6MB
        String bigBase64 = Base64.getEncoder().encodeToString(bigData);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        assertFalse(validator.isValid(bigBase64, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("La imagen no puede superar los 5MB");
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    @DisplayName("Debe retornar true cuando la imagen con prefijo data:image no supera el límite")
    void shouldReturnTrue_whenBase64WithDataPrefix_isUnderLimit() {
        String smallBase64 = Base64.getEncoder().encodeToString(new byte[100]);
        String withPrefix = "data:image/png;base64," + smallBase64;

        assertTrue(validator.isValid(withPrefix, context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Debe retornar false cuando la imagen con prefijo data:image supera el límite")
    void shouldReturnFalse_whenBase64WithDataPrefix_exceedsLimit() {
        byte[] bigData = new byte[6 * 1024 * 1024]; // 6MB
        String bigBase64 = Base64.getEncoder().encodeToString(bigData);
        String withPrefix = "data:image/jpeg;base64," + bigBase64;

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        assertFalse(validator.isValid(withPrefix, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("La imagen no puede superar los 5MB");
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    @DisplayName("Debe retornar true cuando el base64 es inválido (catch IllegalArgumentException)")
    void shouldReturnTrue_whenBase64IsInvalid() {
        String invalidBase64 = "esto-no-es-base64-####";

        assertTrue(validator.isValid(invalidBase64, context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Debe retornar true cuando la imagen pesa exactamente 5MB (límite justo)")
    void shouldReturnTrue_whenImageIsExactlyAtLimit() {
        byte[] exactData = new byte[5 * 1024 * 1024]; // exactamente 5MB
        String exactBase64 = Base64.getEncoder().encodeToString(exactData);

        assertTrue(validator.isValid(exactBase64, context));
        verifyNoInteractions(context);
    }
}