package com.code.crafters.dto.request;

import com.code.crafters.validation.MaxFileSize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
                @NotBlank(message = "El nombre es obligatorio") String name,

                @NotBlank(message = "El primer apellido es obligatorio") String firstName,

                String secondName,

                @NotBlank(message = "El alias es obligatorio") 
                @Size(min = 3, max = 20, message = "El alias debe tener entre 3 y 20 carácteres") String alias,

                @Email(message = "Email inválido") @NotBlank(message = "El email es obligatorio") String email,

                @NotBlank(message = "La contraseña es obligatoria") @Size(min = 6, message = "Mínimo 6 carácteres") String password,

                @MaxFileSize(maxMB = 5) String profileImage) {

}
