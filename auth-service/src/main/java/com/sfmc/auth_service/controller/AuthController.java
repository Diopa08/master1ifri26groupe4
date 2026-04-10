package com.sfmc.auth_service.controller;

import org.springframework.web.bind.annotation.*;

import com.sfmc.auth_service.dto.LoginRequest;
import com.sfmc.auth_service.dto.AuthResponse;
import com.sfmc.auth_service.entity.User;
import com.sfmc.auth_service.repository.UserRepository;
import com.sfmc.auth_service.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow();

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(
                user.getEmail()
        );

        return new AuthResponse(token);
    }
}