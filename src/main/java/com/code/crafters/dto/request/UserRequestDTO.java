package com.code.crafters.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El primer apellido es obligatorio") String firstName,
        String secondName,
        String alias,
        @Email @NotBlank(message = "El email es obligatorio") String email,
        @NotBlank @Size(min = 6, message = "Mínimo 6 caracteres") String password,
        String profileImage) {

}
