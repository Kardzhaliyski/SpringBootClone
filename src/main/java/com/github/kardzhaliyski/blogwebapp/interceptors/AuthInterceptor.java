package com.github.kardzhaliyski.blogwebapp.interceptors;

import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.security.AuthToken;
import com.github.kardzhaliyski.blogwebapp.security.AuthenticationService;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.springbootclone.annotations.Component;
import com.github.kardzhaliyski.springbootclone.exceptions.ResponseStatusException;
import com.github.kardzhaliyski.springbootclone.interceptors.HandlerInterceptor;
import com.github.kardzhaliyski.springbootclone.server.HandlerMethod;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    AuthenticationService authService;

    public AuthInterceptor(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler; //todo
        Role roleAnn = handlerMethod.getMethodAnnotation(Role.class);
        UserRole methodRoleRequirement = roleAnn != null ? roleAnn.value() : UserRole.ADMIN;

        if (methodRoleRequirement.equals(UserRole.VISITOR)) {
            return true;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME);
        AuthToken token = authService.getToken(authHeader);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (token.role.ordinal() >= methodRoleRequirement.ordinal()) {
            return true;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
}
