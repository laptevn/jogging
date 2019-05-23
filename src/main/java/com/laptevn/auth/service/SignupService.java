package com.laptevn.auth.service;

import com.laptevn.auth.entity.Role;
import com.laptevn.auth.entity.SignupDto;
import com.laptevn.auth.entity.UserDto;
import org.springframework.stereotype.Component;

@Component
public class SignupService {
    private final UserService userService;

    public SignupService(UserService userService) {
        this.userService = userService;
    }

    public boolean createUser(SignupDto signupDto) {
        return userService.createUser(
                new UserDto()
                        .setName(signupDto.getName())
                        .setPassword(signupDto.getPassword())
                        .setRole(Role.USER));
    }
}