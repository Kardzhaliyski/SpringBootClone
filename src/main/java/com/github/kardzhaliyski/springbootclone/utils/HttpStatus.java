package com.github.kardzhaliyski.springbootclone.utils;

public enum HttpStatus {
    OK(200),
    CREATED(201),
    BAD_REQUEST (400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    CONFLICT(409);

    int code;
    HttpStatus(int code) {
        this.code = code;
    }
}
