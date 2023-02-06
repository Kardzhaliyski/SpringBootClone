package com.github.kardzhaliyski.blogwebapp.interceptors;


import com.github.kardzhaliyski.springbootclone.annotations.Configuration;
import com.github.kardzhaliyski.springbootclone.interceptors.InterceptorRegistry;
import com.github.kardzhaliyski.springbootclone.interceptors.WebMvcConfigurer;

@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer { //todo
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
