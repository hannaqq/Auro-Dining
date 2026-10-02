package com.aurodining.service.impl;

import lombok.RequiredArgsConstructor;

import com.aurodining.entity.User;
import com.aurodining.repository.UserRepository;
import com.aurodining.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}