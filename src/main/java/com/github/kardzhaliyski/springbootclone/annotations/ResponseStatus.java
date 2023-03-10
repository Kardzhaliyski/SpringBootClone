package com.github.kardzhaliyski.springbootclone.annotations;

import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseStatus {
    HttpStatus value();
}
