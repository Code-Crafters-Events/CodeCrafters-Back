package com.code.crafters.dto.response;

public record AuthResponseDTO(
        String token,
        Long id,
        String name,
        String firstName,
        String secondName,
        String alias,
        String email,
        String profileImage) {
}