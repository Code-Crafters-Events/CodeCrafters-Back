package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.code.crafters.dto.request.LoginRequestDTO;
import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.dto.response.AuthResponseDTO;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.UserMapper;
import com.code.crafters.repository.UserRepository;
import com.code.crafters.security.JwtService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
@SuppressWarnings("null")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("juan@example.com");
        user.setPassword("encoded-password");
        user.setName("Juan");
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterSuccessfully() {
        UserRequestDTO dto = new UserRequestDTO(
                "Juan", "Perez", null, "juanp",
                "juan@example.com", "password123", null);
        AuthResponseDTO expected = new AuthResponseDTO(
                "jwt-token", 1L, "Juan", "Perez", null, "juanp", "juan@example.com", null);
        when(userService.create(dto)).thenReturn(user);
        when(jwtService.generateToken(1L, "juan@example.com")).thenReturn("jwt-token");
        when(userMapper.toAuthResponse(user, "jwt-token")).thenReturn(expected);
        AuthResponseDTO result = authService.register(dto);
        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        verify(userService).create(dto);
    }

    @Test
    @DisplayName("Should throw when email already registered")
    void shouldThrowWhenEmailAlreadyRegistered() {
        UserRequestDTO dto = new UserRequestDTO(
                "Juan", "Perez", null, "juanp",
                "juan@example.com", "password123", null);
        when(userService.create(dto)).thenThrow(new ResourceAlreadyExistsException("Email ya existe"));
        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(dto));
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        LoginRequestDTO dto = new LoginRequestDTO("juan@example.com", "password123");

        AuthResponseDTO expected = new AuthResponseDTO(
                "jwt-token", 1L, "Juan", "Perez", null, "juanp", "juan@example.com", null);

        when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(1L, "juan@example.com")).thenReturn("jwt-token");
        when(userMapper.toAuthResponse(user, "jwt-token")).thenReturn(expected);

        AuthResponseDTO result = authService.login(dto);

        assertNotNull(result);
        assertEquals("jwt-token", result.token());
    }

    @Test
    @DisplayName("Should throw when user not found on login")
    void shouldThrowWhenUserNotFoundOnLogin() {
        LoginRequestDTO dto = new LoginRequestDTO("noexiste@example.com", "password123");

        when(userRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("Should throw when password is incorrect")
    void shouldThrowWhenPasswordIsIncorrect() {
        LoginRequestDTO dto = new LoginRequestDTO("juan@example.com", "wrong-password");

        when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(SecurityException.class, () -> authService.login(dto));
    }
}