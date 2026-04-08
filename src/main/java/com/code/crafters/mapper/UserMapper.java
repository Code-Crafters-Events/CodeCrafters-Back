package com.code.crafters.mapper;

import org.mapstruct.Mapper;

import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.dto.response.UserResponseDTO;
import com.code.crafters.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequestDTO dto);

    UserResponseDTO toResponse(User user);
}
