package com.code.crafters.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.code.crafters.entity.enums.EventCategory;

public record EventFilterDTO(
        String title,
        String authorName,
        EventCategory category,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal priceMin,
        BigDecimal priceMax) {

}
