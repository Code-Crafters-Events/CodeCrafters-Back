package com.code.crafters.dto.response;

public record AuthResponseDTO(
        String token,
        Long id,
        String name,
        String email,
        String profileImage) {
}