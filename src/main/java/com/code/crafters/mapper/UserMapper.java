package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.dto.response.UserResponseDTO;
import com.code.crafters.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    User toEntity(UserRequestDTO dto);

    UserResponseDTO toResponse(User user);
}
