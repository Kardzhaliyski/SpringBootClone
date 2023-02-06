package com.github.kardzhaliyski.springbootclone.exceptions;

import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;

public class ResponseStatusException extends RuntimeException {
    public HttpStatus httpStatus;
    public String msg;

    public ResponseStatusException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ResponseStatusException(HttpStatus httpStatus, String msg) {
        this.httpStatus = httpStatus;
        this.msg = msg;
    }
}
