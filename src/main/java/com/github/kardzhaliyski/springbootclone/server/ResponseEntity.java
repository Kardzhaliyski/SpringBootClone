package com.github.kardzhaliyski.springbootclone.server;

import com.github.kardzhaliyski.springbootclone.utils.HttpHeaders;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;

public class ResponseEntity<T> {

    public HttpHeaders headers;
    public HttpStatus status;
    public T body;

    public ResponseEntity(HttpHeaders headers, HttpStatus status) {
        this.headers = headers;
        this.status = status;
    }

    public ResponseEntity(T body, HttpStatus status) {
        this.body = body;
        this.status = status;
    }


}
