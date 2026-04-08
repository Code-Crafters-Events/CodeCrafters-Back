package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.entity.Ticket;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.title", target = "eventTitle")
    TicketResponseDTO toResponse(Ticket ticket);
}
