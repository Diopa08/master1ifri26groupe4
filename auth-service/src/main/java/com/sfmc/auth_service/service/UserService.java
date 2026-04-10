package com.sfmc.auth_service.service;

import org.springframework.stereotype.Service;

import com.sfmc.auth_service.repository.UserRepository;
import com.sfmc.auth_service.entity.User;

@Service

public class UserService {

    private final UserRepository userRepository;

    // constructeur manuel (sans Lombok)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

}