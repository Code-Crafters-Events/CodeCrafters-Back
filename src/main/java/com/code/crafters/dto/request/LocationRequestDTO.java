package com.code.crafters.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LocationRequestDTO(
        @NotBlank(message = "El nombre del lugar es obligatorio") String venue,
        String address,
        String city,
        String province,
        String country,
        String zipCode,
        Double latitude,
        Double longitude) {

}
