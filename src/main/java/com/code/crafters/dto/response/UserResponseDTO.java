package com.code.crafters.dto.response;

public record UserResponseDTO(
        Long id,
        String name,
        String firstName,
        String secondName,
        String alias,
        String email,
        String profileImage) {

}
