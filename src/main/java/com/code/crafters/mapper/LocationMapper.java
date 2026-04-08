package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.code.crafters.dto.request.LocationRequestDTO;
import com.code.crafters.dto.response.LocationResponseDTO;
import com.code.crafters.entity.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationRequestDTO dto);

    LocationResponseDTO toResponse(Location location);
}
