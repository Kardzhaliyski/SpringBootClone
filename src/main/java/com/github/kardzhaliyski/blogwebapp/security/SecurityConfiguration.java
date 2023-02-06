package com.github.kardzhaliyski.blogwebapp.security;

import com.github.kardzhaliyski.springbootclone.annotations.Bean;
import com.github.kardzhaliyski.springbootclone.annotations.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
