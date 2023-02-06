package com.github.kardzhaliyski.blogwebapp.controllers;

import com.github.kardzhaliyski.blogwebapp.mappers.UserMapper;
import com.github.kardzhaliyski.blogwebapp.models.User;
import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.models.dto.LoginUserDTO;
import com.github.kardzhaliyski.blogwebapp.models.dto.RegisterUserDTO;
import com.github.kardzhaliyski.blogwebapp.security.AuthToken;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.blogwebapp.services.LoginService;
import com.github.kardzhaliyski.blogwebapp.services.UserService;
import com.github.kardzhaliyski.springbootclone.annotations.*;
import com.github.kardzhaliyski.springbootclone.classes.ResponseEntity;
import com.github.kardzhaliyski.springbootclone.utils.HttpHeaders;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;
import com.github.kardzhaliyski.springbootclone.utils.ResponseStatusException;

import java.util.Optional;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final LoginService loginService;

    public AuthController(UserMapper userMapper, UserService userService, LoginService loginService) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.loginService = loginService;
    }

    @PostMapping("login")
    @Role(UserRole.VISITOR)
    public ResponseEntity<?> login(@RequestBody LoginUserDTO dto) {
        if (dto == null || dto.uname == null || dto.psw == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials");
        }

        String token = loginService.login(dto);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, AuthToken.TOKEN_HEADER_PREFIX + token);
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @GetMapping("/register")
    @Role(UserRole.VISITOR)
    public String registerPage() {
        return "register page";
    }

    @PostMapping("/register")
    @Role(UserRole.VISITOR)
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterUserDTO userDto) {
        if (!userDto.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Optional<User> userFromDB = userMapper.getByUsername(userDto.uname);
        if (userFromDB.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this username already exists!");
        }

        userService.registerUser(userDto);
    }
}
