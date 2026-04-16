package com.code.crafters.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.code.crafters.dto.response.PaymentIntentResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;
import com.stripe.model.PaymentIntent;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    
    @Mapping(target = "clientSecret", source = "intent.clientSecret")
    @Mapping(target = "paymentIntentId", source = "intent.id")
    @Mapping(target = "amount", source = "event.price")
    @Mapping(target = "currency", constant = "eur")
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "qrUrl", source = "ticket.qrUrl")
    @Mapping(target = "verificationCode", source = "ticket.verificationCode")
    PaymentIntentResponseDTO toResponse(PaymentIntent intent, Event event, Ticket ticket);

    @Mapping(target = "clientSecret", ignore = true)
    @Mapping(target = "paymentIntentId", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", constant = "eur")
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "qrUrl", source = "ticket.qrUrl")
    @Mapping(target = "verificationCode", source = "ticket.verificationCode")
    PaymentIntentResponseDTO toFreeResponse(Ticket ticket, BigDecimal amount);
}