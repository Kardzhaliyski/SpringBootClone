package com.github.kardzhaliyski.blogwebapp.interceptors;

import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.security.AuthToken;
import com.github.kardzhaliyski.blogwebapp.security.AuthenticationService;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    AuthenticationService authService;

    public AuthInterceptor(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
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
