package com.github.kardzhaliyski.springbootclone.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeleteMapping {
    String[] value();
}
