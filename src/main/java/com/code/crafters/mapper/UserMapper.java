package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.code.crafters.dto.request.UpdateUserRequestDTO;
import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.dto.response.AuthResponseDTO;
import com.code.crafters.dto.response.UserResponseDTO;
import com.code.crafters.entity.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    User toEntity(UserRequestDTO dto);

    UserResponseDTO toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    void updateEntity(UserRequestDTO dto, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    void updateEntityFromProfile(UpdateUserRequestDTO dto, @MappingTarget User user);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "secondName", source = "user.secondName")
    @Mapping(target = "alias", source = "user.alias")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "profileImage", source = "user.profileImage")
    AuthResponseDTO toAuthResponse(User user, String token);
}