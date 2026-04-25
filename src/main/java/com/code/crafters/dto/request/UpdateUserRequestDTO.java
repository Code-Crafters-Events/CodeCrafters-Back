package com.code.crafters.dto.request;

import com.code.crafters.validation.MaxFileSize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El primer apellido es obligatorio") String firstName,
        String secondName,
        String alias,
        @MaxFileSize(maxMB = 5) String profileImage,
        @Size(min = 6, message = "Si cambias la contraseña, debe tener al menos 6 carácteres") String password,
        @Email(message = "Formato de email incorrecto") String email) {

}
