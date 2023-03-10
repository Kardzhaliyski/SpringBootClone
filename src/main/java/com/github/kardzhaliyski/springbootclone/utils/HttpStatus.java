package com.github.kardzhaliyski.springbootclone.utils;

public enum HttpStatus {
    OK(200),
    CREATED(201),
    BAD_REQUEST (400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    CONFLICT(409),
    UNSUPPORTED_MEDIA_TYPE(415),
    INTERNAL_SERVER_ERROR(500);

    int code;
    HttpStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
