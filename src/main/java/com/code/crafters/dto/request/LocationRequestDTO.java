package com.code.crafters.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LocationRequestDTO(
                @NotBlank(message = "El nombre del lugar es obligatorio") String venue,
                @NotBlank(message = "La dirección es obligatoria") String address,
                @NotBlank(message = "La ciudad es obligatoria") String city,
                String province,
                @NotBlank(message = "El país es obligatorio") String country,
                @Pattern(regexp = "^[0-9]{5}$", message = "Código postal debe ser de 5 dígitos") String zipCode,
                @Min(-90) @Max(90) Double latitude,
                @Min(-180) @Max(180) Double longitude) {

}
