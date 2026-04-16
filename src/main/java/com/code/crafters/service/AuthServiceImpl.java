package com.code.crafters.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.code.crafters.dto.request.LoginRequestDTO;
import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.dto.response.AuthResponseDTO;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ResourceAlreadyExistsException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.UserMapper;
import com.code.crafters.repository.UserRepository;
import com.code.crafters.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public AuthResponseDTO register(UserRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email ya registrado");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId(), saved.getEmail());
        return userMapper.toAuthResponse(saved, token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new SecurityException("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return userMapper.toAuthResponse(user, token);
    }
}
