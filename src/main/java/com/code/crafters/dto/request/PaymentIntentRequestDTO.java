package com.code.crafters.dto.request;

import jakarta.validation.constraints.NotNull;

public record PaymentIntentRequestDTO(
                @NotNull(message = "ID de usuario requerido") Long userId,
                @NotNull(message = "ID de evento requerido") Long eventId) {

}
