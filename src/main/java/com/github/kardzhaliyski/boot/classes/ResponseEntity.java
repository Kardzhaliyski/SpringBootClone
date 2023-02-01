package com.github.kardzhaliyski.boot.classes;

import com.github.kardzhaliyski.boot.utils.HttpHeaders;
import com.github.kardzhaliyski.boot.utils.HttpStatus;

public class ResponseEntity<T> {
    public ResponseEntity(HttpHeaders headers, HttpStatus ok) {

    }

    public ResponseEntity(T body, HttpStatus ok) {

    }
}
