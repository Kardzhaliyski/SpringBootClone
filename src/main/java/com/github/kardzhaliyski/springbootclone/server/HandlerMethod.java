package com.github.kardzhaliyski.springbootclone.server;

import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.springbootclone.server.RequestHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Matcher;

public class HandlerMethod {
    RequestHandler requestHandler;
    Matcher matcher;

    public HandlerMethod(RequestHandler requestHandler, Matcher matcher) {
        this.requestHandler = requestHandler;
        this.matcher = matcher;
    }

    public <T extends Annotation> T getMethodAnnotation(Class<T> annotation) {
        return requestHandler.getMethod().getAnnotation(annotation);
    }
}
