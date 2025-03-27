package com.taxirank.backend.services;

import java.util.List;

import com.taxirank.backend.dto.UserDTO;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;

public interface UserService {
    List<User> getAllUsers();
    List<User> getUsersByRole(UserRole role);
    User getUserById(Long id);
    User createUser(UserDTO userDTO);
    User updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
} 