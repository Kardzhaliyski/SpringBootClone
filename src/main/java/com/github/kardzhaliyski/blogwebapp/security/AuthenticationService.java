package com.github.kardzhaliyski.blogwebapp.security;

import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.springbootclone.annotations.Service;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.github.kardzhaliyski.blogwebapp.security.AuthToken.TOKEN_HEADER_PREFIX;

@Service
public class AuthenticationService {


    public static AuthenticationService instance = null;
    private final AuthTokenMapper tokenMapper;
    private final Random random = new Random();
    private final Map<String, AuthToken> tokenMap = new HashMap<>();

    public AuthenticationService(AuthTokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    private boolean isValidToken(String token) {
        if (tokenMap.containsKey(token)) {
            return true;
        }

        AuthToken authToken = tokenMapper.getBy(token);
        if (authToken == null) {
            return false;
        }

        tokenMap.put(token, authToken);
        return true;
    }

    public String createNewToken(String username, UserRole userRole) {
        int salt = random.nextInt();
        String token = DigestUtils.sha1Hex(username + "$" + salt);
        AuthToken authToken = new AuthToken(token, username, userRole);
        tokenMapper.addToken(authToken);
        tokenMap.put(token, authToken);
        return token;
    }

    public AuthToken getToken(String authHeader) {
        if (authHeader == null) {
            return null;
        }

        boolean correctSchema = authHeader.startsWith(TOKEN_HEADER_PREFIX);
        if (!correctSchema) {
            return null;
        }

        String token = authHeader.substring(TOKEN_HEADER_PREFIX.length());
        return isValidToken(token) ? tokenMap.get(token) : null;
    }
}
