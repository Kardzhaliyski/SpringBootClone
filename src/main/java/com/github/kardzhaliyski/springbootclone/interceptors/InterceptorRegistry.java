package com.github.kardzhaliyski.springbootclone.interceptors;

import com.github.kardzhaliyski.springbootclone.annotations.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class InterceptorRegistry {

    List<HandlerInterceptor> interceptors = new ArrayList<>();
    public void addInterceptor(HandlerInterceptor interceptor) {
        if(interceptor == null) {
            throw new IllegalArgumentException("Interceptor should not be null.");
        }

        interceptors.add(interceptor);
    }

    public List<HandlerInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }


}
