package com.code.crafters.service;

import java.util.List;

import com.code.crafters.dto.request.UserRequestDTO;
import com.code.crafters.entity.User;

public interface UserService {
    User create(UserRequestDTO dto);

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateUser(Long id, UserRequestDTO dto);

    void deleteUser(Long id);
}
