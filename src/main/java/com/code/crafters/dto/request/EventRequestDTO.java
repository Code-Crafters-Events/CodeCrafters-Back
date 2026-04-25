package com.code.crafters.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.validation.MaxFileSize;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequestDTO(
        @NotBlank(message = "El título es obligatorio") @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres") String title,

        @NotBlank(message = "La descripción es obligatoria") String description,

        @NotNull(message = "El tipo es obligatorio") EventType type,

        @NotNull(message = "La fecha es obligatoria") @Future(message = "La fecha debe ser futura") LocalDate date,

        @NotNull(message = "La hora es obligatoria") LocalTime time,

        @NotNull(message = "El número máximo de asistentes es obligatorio") @Min(value = 1, message = "Debe haber al menos 1 plaza") Integer maxAttendees,

        Long locationId,

        @NotNull(message = "La categoría es obligatoria") EventCategory category,

        @NotNull(message = "El precio es obligatorio") @DecimalMin(value = "0.0", message = "El precio no puede ser negativo") BigDecimal price,

        @MaxFileSize(maxMB = 5) String imageUrl) {

}
