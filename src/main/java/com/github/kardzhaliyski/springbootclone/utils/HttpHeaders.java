package com.github.kardzhaliyski.springbootclone.utils;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {
    public static String AUTHORIZATION = "Authorization";
    public static String CONTENT_TYPE = "Content-Type";

    private Map<String, String> headers = new HashMap<>();

    public void add(String name, String value) {
        headers.put(name, value);
    }

    public String get(String name) {
        return headers.get(name);
    }
}
