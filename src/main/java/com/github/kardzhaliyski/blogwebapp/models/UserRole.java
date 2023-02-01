package com.github.kardzhaliyski.blogwebapp.models;

public enum UserRole {
    VISITOR,
    USER,
    ADMIN;

    public String getName(){
        return this.name();
    }
}
