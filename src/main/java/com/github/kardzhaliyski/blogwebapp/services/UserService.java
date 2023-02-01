package com.github.kardzhaliyski.blogwebapp.services;

import com.github.kardzhaliyski.blogwebapp.mappers.UserMapper;
import com.github.kardzhaliyski.blogwebapp.models.User;
import com.github.kardzhaliyski.blogwebapp.models.dto.RegisterUserDTO;
import com.github.kardzhaliyski.boot.annotations.Service;
import com.github.kardzhaliyski.boot.interfaces.PasswordEncoder;

@Service
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserMapper userMapper;

    public UserService(PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public void registerUser(RegisterUserDTO userDto) {
        String password = passwordEncoder.encode(userDto.psw);
        User user = new User(userDto.uname, userDto.fName, userDto.lName, userDto.email, password);
        userMapper.insert(user);
    }
}
