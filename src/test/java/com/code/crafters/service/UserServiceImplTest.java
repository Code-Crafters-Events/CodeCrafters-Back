package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

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
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.UserMapper;
import com.code.crafters.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
@SuppressWarnings("null")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRequestDTO userRequestDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Juan");
        testUser.setFirstName("Pérez");
        testUser.setEmail("juan@example.com");
        testUser.setAlias("juanperez");
        testUser.setPassword("encodedPassword123");

        userRequestDTO = new UserRequestDTO(
                "Juan",
                "Pérez",
                null,
                "juanperez",
                "juan@example.com",
                "password123",
                null);
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
        when(userRepository.existsByAliasIgnoreCase(userRequestDTO.alias())).thenReturn(false);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User result = userService.create(userRequestDTO);
        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testCreateUserEmailAlreadyExists() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.create(userRequestDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when alias already exists")
    void testCreateUserAliasAlreadyExists() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
        when(userRepository.existsByAliasIgnoreCase(userRequestDTO.alias())).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.create(userRequestDTO));
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void testGetUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        User result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("juan@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("otro@example.com");
        when(userRepository.findAll()).thenReturn(List.of(testUser, user2));
        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User result = userService.updateUser(1L, userRequestDTO);
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfileSuccess() {
        UpdateUserRequestDTO updateDTO = new UpdateUserRequestDTO(
                "Juan", "Pérez García", "García", "juanperez", null, null, "juan.perez@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User result = userService.updateProfile(1L, updateDTO);
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to existing email")
    void testUpdateProfileEmailAlreadyExists() {
        UpdateUserRequestDTO updateDTO = new UpdateUserRequestDTO(
                "Juan", "Pérez", null, "juanperez", null, null, "otro@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("otro@example.com")).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateProfile(1L, updateDTO));
    }
}