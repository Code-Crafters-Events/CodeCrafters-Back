package com.code.crafters.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequestDTO(
        @NotBlank(message = "El título es obligatorio") String title,
        String description,
        @NotNull(message = "El tipo es obligatorio") EventType type,
        @NotNull(message = "La fecha es obligatoria") LocalDate date,
        LocalTime time,
        Integer maxAttendees,
        Long locationId,
        @NotNull(message = "La categoría es obligatoria") EventCategory category,
        BigDecimal price,
        String imageUrl) {

}
