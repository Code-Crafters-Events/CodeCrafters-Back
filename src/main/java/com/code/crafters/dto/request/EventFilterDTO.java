package com.code.crafters.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.code.crafters.entity.enums.EventCategory;

import jakarta.validation.constraints.DecimalMin;

public record EventFilterDTO(
                String title,
                String authorAlias,
                EventCategory category,
                LocalDate dateFrom,
                LocalDate dateTo,
                @DecimalMin(value = "0.0") BigDecimal priceMin,
                @DecimalMin(value = "0.0") BigDecimal priceMax) {

}
