package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.title", target = "eventTitle")
    TicketResponseDTO toResponse(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "paymentIntentId", source = "paymentIntentId")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    Ticket toEntity(User user, Event event, String paymentIntentId, PaymentStatus paymentStatus);
}