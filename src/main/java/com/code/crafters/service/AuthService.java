package com.code.crafters.service;

import com.code.crafters.dto.request.LoginRequestDTO;
import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(UserRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);
}
