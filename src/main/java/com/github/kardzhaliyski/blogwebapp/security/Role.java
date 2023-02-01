package com.github.kardzhaliyski.blogwebapp.security;

import com.github.kardzhaliyski.blogwebapp.models.UserRole;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Role {
    UserRole value();
}
