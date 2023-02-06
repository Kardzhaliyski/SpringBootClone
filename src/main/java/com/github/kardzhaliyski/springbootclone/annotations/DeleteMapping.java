package com.github.kardzhaliyski.springbootclone.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//@RequestMapping(method = RequestMethod.DELETE) //todo
public @interface DeleteMapping {
    String[] value();
}
