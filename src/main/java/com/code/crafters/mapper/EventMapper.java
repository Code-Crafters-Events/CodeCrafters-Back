package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.entity.Event;

@Mapper(componentModel = "spring", uses = { LocationMapper.class })
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    Event toEntity(EventRequestDTO dto);

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "author.alias", target = "authorAlias")
    @Mapping(target = "attendeesCount", expression = "java(event.getTickets() != null ? event.getTickets().size() : 0)")
    EventResponseDTO toResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    void updateEntity(EventRequestDTO dto, @MappingTarget Event event);
}