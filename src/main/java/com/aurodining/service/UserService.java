package com.aurodining.service;

import com.aurodining.dto.LoginResult;
import com.aurodining.entity.User;

public interface UserService {

    void sendVerificationCode(String email);

    LoginResult<User> loginByEmailCode(String email, String code);

    // Find user by email for login identity
    User getByEmail(String email);

    // Save or update user
    User save(User user);

    // 🔥 Add this: Find user by ID for order processing
    User getById(Long id);
}