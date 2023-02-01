package com.github.kardzhaliyski.blogwebapp.models.dto;

import com.github.kardzhaliyski.blogwebapp.models.UserRole;

public class ChangeUserRoleDTO {
    public String uname;
    public UserRole role;

    public void setRole(String role) {
        this.role = UserRole.valueOf(role.toUpperCase());
    }
}
