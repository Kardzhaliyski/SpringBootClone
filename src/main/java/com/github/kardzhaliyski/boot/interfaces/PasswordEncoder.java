package com.github.kardzhaliyski.boot.interfaces;

public interface PasswordEncoder {
    boolean matches(String raw, String encoded);

    String encode(String raw);
}
