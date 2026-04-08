package com.code.crafters.dto.response;

import java.time.LocalDateTime;

public record TicketResponseDTO(
        Long id,
        LocalDateTime createdAt,
        Long userId,
        String userName,
        Long eventId,
        String eventTitle) {

}
