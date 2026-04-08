package com.code.crafters.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.UserMapper;
import com.code.crafters.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User create(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email()))
            throw new ResourceAlreadyExistsException("Email ya registrado: " + dto.email());
        if (dto.alias() != null && userRepository.existsByAlias(dto.alias()))
            throw new ResourceAlreadyExistsException("Alias ya en uso: " + dto.alias());
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, UserRequestDTO dto) {
        User user = getUserById(id);
        user.setName(dto.name());
        user.setFirstName(dto.firstName());
        user.setSecondName(dto.secondName());
        user.setAlias(dto.alias());
        user.setProfileImage(dto.profileImage());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        getUserById(id);
        userRepository.deleteById(id);
    }
}
