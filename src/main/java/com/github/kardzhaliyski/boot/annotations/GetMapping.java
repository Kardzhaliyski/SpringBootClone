package com.github.kardzhaliyski.boot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//@RequestMapping(method = RequestMethod.GET) //todo
public @interface GetMapping {
    String[] value() default "";

    String[] params() default "";
}
