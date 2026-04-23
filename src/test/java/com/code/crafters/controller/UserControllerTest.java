package com.code.crafters.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.code.crafters.dto.response.UserResponseDTO;
import com.code.crafters.entity.User;
import com.code.crafters.exception.GlobalExceptionHandler;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.UserMapper;
import com.code.crafters.service.UserService;

@SuppressWarnings("null")
@DisplayName("UserController Tests")
class UserControllerTest {

        private MockMvc mockMvc;
        private UserService userService;
        private UserMapper userMapper;

        @BeforeEach
        void setUp() {
                userService = org.mockito.Mockito.mock(UserService.class);
                userMapper = org.mockito.Mockito.mock(UserMapper.class);

                UserController controller = new UserController(userService, userMapper);

                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        void shouldCreateUserSuccessfully() throws Exception {
                User user = new User();
                user.setId(1L);

                UserResponseDTO response = new UserResponseDTO(
                                1L, "Juan", "Perez", "Garcia", "juanp", "juan@example.com", null);

                when(userService.create(any())).thenReturn(user);
                when(userMapper.toResponse(user)).thenReturn(response);

                String body = """
                                {
                                  "name":"Juan",
                                  "firstName":"Perez",
                                  "secondName":"Garcia",
                                  "alias":"juanp",
                                  "email":"juan@example.com",
                                  "password":"password123",
                                  "profileImage":null
                                }
                                """;

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.email").value("juan@example.com"));
        }

        @Test
        void shouldGetAllUsers() throws Exception {
                User user = new User();

                UserResponseDTO response = new UserResponseDTO(
                                1L, "Juan", "Perez", "Garcia", "juanp", "juan@example.com", null);

                when(userService.getAllUsers()).thenReturn(List.of(user));
                when(userMapper.toResponse(user)).thenReturn(response);

                mockMvc.perform(get("/api/v1/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        void shouldGetUserById() throws Exception {
                User user = new User();

                UserResponseDTO response = new UserResponseDTO(
                                1L, "Juan", "Perez", "Garcia", "juanp", "juan@example.com", null);

                when(userService.getUserById(1L)).thenReturn(user);
                when(userMapper.toResponse(user)).thenReturn(response);

                mockMvc.perform(get("/api/v1/users/{id}", 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
                when(userService.getUserById(99L))
                                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

                mockMvc.perform(get("/api/v1/users/{id}", 99L))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldUpdateUser() throws Exception {
                User user = new User();

                UserResponseDTO response = new UserResponseDTO(
                                1L, "Juan Carlos", "Perez", "Garcia", "juanp", "juan@example.com", null);

                when(userService.updateUser(eq(1L), any())).thenReturn(user);
                when(userMapper.toResponse(user)).thenReturn(response);

                String body = """
                                {
                                  "name":"Juan Carlos",
                                  "firstName":"Perez",
                                  "secondName":"Garcia",
                                  "alias":"juanp",
                                  "email":"juan@example.com",
                                  "password":"password123",
                                  "profileImage":null
                                }
                                """;

                mockMvc.perform(put("/api/v1/users/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Juan Carlos"));
        }

        @Test
        void shouldDeleteUser() throws Exception {
                doNothing().when(userService).deleteUser(1L);

                mockMvc.perform(delete("/api/v1/users/{id}", 1L))
                                .andExpect(status().isNoContent());
        }

        @Test
        void shouldPatchUserProfile() throws Exception {
                User user = new User();

                UserResponseDTO response = new UserResponseDTO(
                                1L, "Juan", "Perez", "Garcia", "nuevoalias", "juan@example.com", "avatar.png");

                when(userService.updateProfile(eq(1L), any())).thenReturn(user);
                when(userMapper.toResponse(user)).thenReturn(response);

                String body = """
                                {
                                  "name":"Juan",
                                  "firstName":"Perez",
                                  "secondName":"Garcia",
                                  "alias":"nuevoalias",
                                  "profileImage":"avatar.png",
                                  "password":null,
                                  "email":"juan@example.com"
                                }
                                """;

                mockMvc.perform(patch("/api/v1/users/{id}/profile", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.alias").value("nuevoalias"));
        }

        @Test
        void shouldReturnBadRequestWhenCreatePayloadIsInvalid() throws Exception {
                String body = """
                                {
                                  "name":"",
                                  "firstName":"",
                                  "email":"bad-email",
                                  "password":"123"
                                }
                                """;

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }
}
