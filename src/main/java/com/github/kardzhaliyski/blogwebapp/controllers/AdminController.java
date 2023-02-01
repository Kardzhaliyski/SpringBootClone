package com.github.kardzhaliyski.blogwebapp.controllers;

import com.github.kardzhaliyski.blogwebapp.mappers.UserMapper;
import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.models.dto.ChangeUserRoleDTO;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.boot.annotations.*;
import com.github.kardzhaliyski.boot.utils.HttpStatus;
import com.github.kardzhaliyski.boot.utils.ResponseStatusException;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserMapper userMapper;

    public AdminController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Role(UserRole.ADMIN)
    @PostMapping("/change_role")
    public void changeUserRole(@RequestBody ChangeUserRoleDTO dto) {
        if(dto == null || dto.uname == null || dto.role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boolean changed = userMapper.changeRole(dto);
        if(!changed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }

    @Role(UserRole.ADMIN)
    @PostMapping("/activate_user")
    public void activateUser(@RequestBody String uname) {
        if(uname == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boolean activated = userMapper.activate(uname);
        if(!activated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }

    @Role(UserRole.ADMIN)
    @PostMapping("/deactivate_user")
    public void deactivateUser(@RequestBody String uname) {
        if(uname == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boolean deactivated = userMapper.deactivate(uname);
        if(!deactivated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }


}
