package com.github.kardzhaliyski.blogwebapp.services;

import com.github.kardzhaliyski.blogwebapp.mappers.UserMapper;
import com.github.kardzhaliyski.blogwebapp.models.User;
import com.github.kardzhaliyski.blogwebapp.models.dto.LoginUserDTO;
import com.github.kardzhaliyski.blogwebapp.security.AuthenticationService;
import com.github.kardzhaliyski.boot.annotations.Service;
import com.github.kardzhaliyski.boot.interfaces.PasswordEncoder;

import java.util.Optional;

@Service
public class LoginService {
    private final UserMapper userMapper;
    private final AuthenticationService authService;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserMapper userMapper, AuthenticationService authService, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(LoginUserDTO dto) {
        Optional<User> userOptional = userMapper.getByUsername(dto.uname);
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        boolean matches = passwordEncoder.matches(dto.psw, user.password);
        if (matches) {
            return authService.createNewToken(dto.uname, user.role);
        }

        return null;
    }

}
