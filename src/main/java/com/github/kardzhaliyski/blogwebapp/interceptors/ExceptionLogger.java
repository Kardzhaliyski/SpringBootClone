package com.github.kardzhaliyski.blogwebapp.interceptors;

import com.github.kardzhaliyski.springbootclone.annotations.Component;
import com.github.kardzhaliyski.springbootclone.interceptors.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ExceptionLogger implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        int status = response.getStatus();
        if (status >= 400 && status < 500) {
            logger.info("Status code: " + response.getStatus() + " For path: " + request.getServletPath());
        } else if (ex != null || status > 500) {
            logger.warn("Status code: " + response.getStatus() + " For path: " + request.getServletPath(), ex);
        }
    }
}
