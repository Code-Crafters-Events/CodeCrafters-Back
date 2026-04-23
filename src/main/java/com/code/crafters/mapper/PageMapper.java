package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;

@Mapper(componentModel = "spring", uses = { EventMapper.class, TicketMapper.class })
public interface PageMapper {

   @Mapping(target = "page", source = "number")
    PageResponseDTO<EventResponseDTO> toEventPageResponse(Page<Event> page);

    @Mapping(target = "page", source = "number")
    PageResponseDTO<TicketResponseDTO> toTicketPageResponse(Page<Ticket> page);
}
