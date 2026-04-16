package com.code.crafters.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El primer apellido es obligatorio") String firstName,
        String secondName,
        String alias,
        String profileImage,
        String password,
        String email) {

}
