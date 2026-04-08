package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.entity.Event;

@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface EventMapper {
@Mapping(target = "author", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    Event toEntity(EventRequestDTO dto);

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.name", target = "authorName")
    EventResponseDTO toResponse(Event event);
}
