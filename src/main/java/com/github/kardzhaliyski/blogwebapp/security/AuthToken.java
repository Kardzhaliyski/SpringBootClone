package com.github.kardzhaliyski.blogwebapp.security;

import com.github.kardzhaliyski.blogwebapp.models.UserRole;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class AuthToken {
    public static final String TOKEN_HEADER_PREFIX = "Bearer ";
    public static final long DEFAULT_EXPIRATION_TIME = 30;
    public static final TemporalUnit DEFAULT_EXPIRATION_UNIT = ChronoUnit.DAYS;
    public String uname;
    public String token;
    public UserRole role;
    public LocalDateTime createdDate;
    public LocalDateTime expirationDate;

    public AuthToken() {
    }

    public AuthToken(String token, String uname, UserRole userRole) {
        this.uname = uname;
        this.token = token;
        this.role = userRole;
        this.createdDate = LocalDateTime.now();
        this.expirationDate = LocalDateTime.now().plus(DEFAULT_EXPIRATION_TIME, DEFAULT_EXPIRATION_UNIT);
    }

    public void setRole(String role) {
        this.role = UserRole.valueOf(role.toUpperCase());
    }
}
