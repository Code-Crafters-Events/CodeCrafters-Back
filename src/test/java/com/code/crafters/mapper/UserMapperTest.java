package com.code.crafters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.code.crafters.dto.request.UpdateUserRequestDTO;
import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.dto.response.AuthResponseDTO;
import com.code.crafters.dto.response.UserResponseDTO;
import com.code.crafters.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapRequestToEntity() {
        UserRequestDTO dto = new UserRequestDTO(
                "Juan",
                "Perez",
                "Garcia",
                "juanp",
                "juan@example.com",
                "password123",
                "avatar.png");

        User user = mapper.toEntity(dto);

        assertEquals("Juan", user.getName());
        assertEquals("Perez", user.getFirstName());
        assertEquals("juanp", user.getAlias());
        assertEquals("juan@example.com", user.getEmail());
    }

    @Test
    void shouldMapEntityToResponse() {
        User user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setFirstName("Perez");
        user.setSecondName("Garcia");
        user.setAlias("juanp");
        user.setEmail("juan@example.com");
        user.setProfileImage("avatar.png");

        UserResponseDTO response = mapper.toResponse(user);

        assertEquals(1L, response.id());
        assertEquals("Juan", response.name());
        assertEquals("juanp", response.alias());
    }

    @Test
    void shouldUpdateEntityFromProfileIgnoringNulls() {
        User user = new User();
        user.setName("Old");
        user.setFirstName("OldFirst");
        user.setSecondName("OldSecond");
        user.setAlias("oldalias");
        user.setEmail("old@example.com");
        user.setProfileImage("old.png");

        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "New",
                "NewFirst",
                null,
                "newalias",
                null,
                null,
                null);

        mapper.updateEntityFromProfile(dto, user);

        assertEquals("New", user.getName());
        assertEquals("NewFirst", user.getFirstName());
        assertEquals("OldSecond", user.getSecondName());
        assertEquals("newalias", user.getAlias());
        assertEquals("old@example.com", user.getEmail());
    }

    @Test
    void shouldMapAuthResponse() {
        User user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setFirstName("Perez");
        user.setSecondName("Garcia");
        user.setAlias("juanp");
        user.setEmail("juan@example.com");
        user.setProfileImage("avatar.png");

        AuthResponseDTO response = mapper.toAuthResponse(user, "token-123");

        assertEquals("token-123", response.token());
        assertEquals(1L, response.id());
        assertEquals("juan@example.com", response.email());
    }
}
