package com.code.crafters.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;

public record EventResponseDTO(
                Long id,
                String title,
                String description,
                EventType type,
                LocalDate date,
                LocalTime time,
                Integer maxAttendees,
                LocationResponseDTO location,
                EventCategory category,
                BigDecimal price,
                String imageUrl,
                Long authorId,
                String authorName,
                String authorAlias) {

}
