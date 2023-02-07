package com.github.kardzhaliyski.springbootclone.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpHeaders implements Iterable<Map.Entry<String, String>> {
    public static String AUTHORIZATION = "Authorization";
    public static String CONTENT_TYPE = "Content-Type";

    private Map<String, String> headers = new HashMap<>();

    public void add(String name, String value) {
        headers.put(name, value);
    }

    public String get(String name) {
        return headers.get(name);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
         return headers.entrySet().iterator();
    }
}
