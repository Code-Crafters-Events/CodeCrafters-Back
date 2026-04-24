package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.code.crafters.dto.request.UpdateUserRequestDTO;
import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.mapper.UserMapper;
import com.code.crafters.repository.UserRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Additional Tests")
class UserServiceImplAdditionalTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setFirstName("Perez");
        user.setSecondName("Garcia");
        user.setAlias("juanp");
        user.setEmail("juan@example.com");
        user.setPassword("encoded");
        user.setProfileImage("old.png");
    }

    @Test
    void shouldCreateUserWithoutAlias() {
        UserRequestDTO dto = new UserRequestDTO(
                "Juan", "Perez", null, null, "juan@example.com", "password123", null);

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);

        userService.create(dto);

        verify(userRepository, never()).existsByAlias(any());
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateProfileEmailWhenAvailable() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", "img.png", null, "nuevo@example.com");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByEmail("nuevo@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertEquals("nuevo@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void shouldFailUpdateProfileWhenAliasAlreadyExists() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "otroalias", "img.png", null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByAlias("otroalias")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateProfile(1L, dto));
    }

    @Test
    void shouldClearProfileImageWhenNull() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertNull(result.getProfileImage());
    }

    @Test
    void shouldKeepProfileImageWhenBlank() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", "   ", null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertEquals("old.png", result.getProfileImage());
    }

    @Test
    void shouldEncodeAndUpdatePassword() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, "newPassword123", null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertEquals("new-encoded", result.getPassword());
    }

    @Test
    void shouldIgnoreMaskedPassword() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, "********", null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertEquals("encoded", result.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldNotCheckEmailWhenEmailIsUnchanged() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, null, "juan@example.com");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(1L, dto);

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void shouldNotCheckAliasWhenAliasIsUnchanged() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(1L, dto);

        verify(userRepository, never()).existsByAlias(any());
    }

    @Test
    void shouldUpdateProfileImageWhenProvidedAndNotBlank() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", "new.png", null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertEquals("new.png", result.getProfileImage());
    }

    @Test
    void shouldIgnoreBlankPassword() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, "   ", null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertEquals("encoded", result.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldFailUpdateProfileWhenEmailAlreadyExists() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, null, "otro@example.com");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByEmail("otro@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateProfile(1L, dto));
    }

    @Test
    void shouldUpdateAliasWhenAvailable() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "nuevoalias", null, null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByAlias("nuevoalias")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(1L, dto);

        verify(userRepository).existsByAlias("nuevoalias");
        verify(userMapper).updateEntityFromProfile(dto, user);
        verify(userRepository).save(user);
    }

    @Test
    void shouldKeepEmailWhenNull() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "juanp", null, null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(1L, dto);

        assertEquals("juan@example.com", result.getEmail());
    }

    @Test
    void shouldCallMapperWhenUpdatingProfile() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", "nuevoalias", "img.png", null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByAlias("nuevoalias")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(1L, dto);

        verify(userMapper).updateEntityFromProfile(dto, user);
    }

    @Test
    @DisplayName("Should skip alias validation when alias in DTO is null")
    void shouldSkipAliasValidationWhenAliasIsNull() {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "Juan", "Perez", "Garcia", null, "img.png", null, null);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        userService.updateProfile(1L, dto);
        verify(userRepository, never()).existsByAlias(anyString());
        verify(userRepository).save(user);
    }

}
