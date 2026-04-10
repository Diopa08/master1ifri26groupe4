package com.sfmc.auth_service.controller;

import org.springframework.web.bind.annotation.*;

import com.sfmc.auth_service.entity.User;
import com.sfmc.auth_service.service.UserService;

@RestController
@RequestMapping("/users")

public class UserController {

    private final UserService userService;

    // constructeur manuel
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(
            @RequestBody User user) {

        return userService.saveUser(user);
    }

}