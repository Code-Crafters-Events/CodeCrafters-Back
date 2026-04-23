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

@DisplayName("LocationRequestDTO Validation Tests")
class LocationRequestDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        LocationRequestDTO dto = new LocationRequestDTO(
                "IFEMA",
                "Av. del Partenon 5",
                "Madrid",
                "Madrid",
                "Espana",
                "28042",
                40.4637,
                -3.6123);

        Set<ConstraintViolation<LocationRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreBlank() {
        LocationRequestDTO dto = new LocationRequestDTO(
                "",
                "",
                "",
                "Madrid",
                "",
                "28042",
                40.4637,
                -3.6123);

        Set<ConstraintViolation<LocationRequestDTO>> violations = validator.validate(dto);

        assertEquals(4, violations.size());
    }

    @Test
    void shouldFailValidationWhenZipCodeIsInvalid() {
        LocationRequestDTO dto = new LocationRequestDTO(
                "IFEMA",
                "Av. del Partenon 5",
                "Madrid",
                "Madrid",
                "Espana",
                "123",
                40.4637,
                -3.6123);

        Set<ConstraintViolation<LocationRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailValidationWhenLatitudeAndLongitudeAreOutOfRange() {
        LocationRequestDTO dto = new LocationRequestDTO(
                "IFEMA",
                "Av. del Partenon 5",
                "Madrid",
                "Madrid",
                "Espana",
                "28042",
                100.0,
                -200.0);

        Set<ConstraintViolation<LocationRequestDTO>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
    }
}
