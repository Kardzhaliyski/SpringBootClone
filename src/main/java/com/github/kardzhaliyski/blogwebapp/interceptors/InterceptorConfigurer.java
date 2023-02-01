package com.github.kardzhaliyski.blogwebapp.interceptors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {
    ExceptionLogger exceptionLogger;
    AuthInterceptor authInterceptor;

    public InterceptorConfigurer(ExceptionLogger exceptionLogger, AuthInterceptor authInterceptor) {
        this.exceptionLogger = exceptionLogger;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
        registry.addInterceptor(exceptionLogger);
    }
}
