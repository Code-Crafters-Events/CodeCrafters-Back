package com.code.crafters.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.dto.response.TicketVerificationResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.PaymentStatus;

@Mapper(componentModel = "spring", imports = { LocalDateTime.class })
public interface TicketMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.title", target = "eventTitle")
    TicketResponseDTO toResponse(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "usedAt", ignore = true)
    @Mapping(target = "qrUrl", ignore = true)
    @Mapping(target = "verificationCode", ignore = true)
    Ticket toEntity(User user, Event event, String paymentIntentId, PaymentStatus paymentStatus);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "usedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "paymentIntentId", ignore = true)
    void updateTicketPayment(PaymentStatus paymentStatus, String verificationCode,
            String qrUrl, @MappingTarget Ticket ticket);

    @Mapping(target = "valid", constant = "false")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "ticketId", ignore = true)
    @Mapping(target = "eventTitle", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "purchasedAt", ignore = true)
    @Mapping(target = "usedAt", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    TicketVerificationResponseDTO toNotFoundResponse(String message);

    @Mapping(target = "valid", source = "valid")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "eventTitle", source = "ticket.event.title")
    @Mapping(target = "userName", source = "ticket.user.name")
    @Mapping(target = "purchasedAt", source = "ticket.createdAt")
    @Mapping(target = "usedAt", source = "ticket.usedAt")
    @Mapping(target = "paymentStatus", source = "ticket.paymentStatus")
    TicketVerificationResponseDTO toVerificationResponse(Ticket ticket, boolean valid, String message);
}