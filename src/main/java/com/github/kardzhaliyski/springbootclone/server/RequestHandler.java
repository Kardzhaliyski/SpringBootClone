package com.github.kardzhaliyski.springbootclone.server;

import com.github.kardzhaliyski.springbootclone.annotations.PathVariable;
import com.github.kardzhaliyski.springbootclone.annotations.RequestBody;
import com.github.kardzhaliyski.springbootclone.annotations.RequestParam;
import com.github.kardzhaliyski.springbootclone.exceptions.ResponseStatusException;
import com.github.kardzhaliyski.springbootclone.utils.HttpHeaders;
import com.github.kardzhaliyski.springbootclone.context.annotations.Qualifier;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class RequestHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Object instance;
    private final Method method;
    private final DispatcherServlet dispatcherServlet;

    public RequestHandler(Object instance, Method method, DispatcherServlet dispatcherServlet) {
        this.instance = instance;
        this.method = method;
        this.dispatcherServlet = dispatcherServlet;
    }

    public void invoke(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        invoke(req, resp, null);
    }

    public void invoke(HttpServletRequest req, HttpServletResponse resp, Matcher matcher) throws Exception {
        Parameter[] paramTypes = method.getParameters();
        Object response;
        if (paramTypes.length == 0) {
            response = method.invoke(instance);
        } else {
            response = invokeMultiParamMethod(req, matcher, paramTypes);
        }


        PrintWriter writer = resp.getWriter();
        writer.println(gson.toJson(response));
        writer.flush();
    }

    private Object invokeMultiParamMethod(HttpServletRequest req, Matcher matcher, Parameter[] paramTypes) throws Exception {
        Object[] params = new Object[paramTypes.length];
        int pathVarIndex = 1;
        Map<String, String> requestParams = null;
        for (int i = 0; i < paramTypes.length; i++) {
            Parameter paramType = paramTypes[i];
            if (paramType.isAnnotationPresent(RequestBody.class)) {
                String contentType = req.getHeader(HttpHeaders.CONTENT_TYPE);
                if (!contentType.equalsIgnoreCase("application/json")) {
                    throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }

                params[i] = gson.fromJson(req.getReader(), paramType.getType());
            } else if (paramType.isAnnotationPresent(PathVariable.class)) {
                if (matcher == null || matcher.groupCount() == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }

                String paramStrValue = matcher.group(pathVarIndex);
                try {
                    params[i] = parseVariable(paramType.getType(), paramStrValue);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
            } else if (paramType.isAnnotationPresent(Qualifier.class)) {
                String qualifier = paramType.getAnnotation(Qualifier.class).value();
                params[i] = dispatcherServlet.getContainer().getInstance(qualifier);
            } else if (paramType.isAnnotationPresent(RequestParam.class)) {
                if (requestParams == null) {
                    String queryString = req.getQueryString();
                    if(queryString == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                    }

                    requestParams = parseRequestParams(queryString);
                }

                String paramName = paramType.getAnnotation(RequestParam.class).value();
                String paramStrValue = requestParams.get(paramName);
                try {
                    params[i] = parseVariable(paramType.getType(), paramStrValue);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
            } else {
                params[i] = dispatcherServlet.getContainer().getInstance(paramType.getType());
            }
        }

        return method.invoke(instance, params);
    }

    private Map<String, String> parseRequestParams(String queryString) {
        int i = queryString.indexOf("#");
        if (i != -1) {
            queryString = queryString.substring(0, i);
        }

        String[] split = queryString.split("&");
        Map<String, String> map = new HashMap<>(); //todo check if order matters
        for (String entry : split) {
            String[] kvp = entry.split("=");
            map.put(kvp[0], kvp[1]);
        }

        return map;
    }

    private Object parseVariable(Class<?> type, String strValue) {
        if (strValue == null) {
            return null;
        }

        if (type.equals(String.class)) {
            return strValue;
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(strValue);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(strValue);
        } else if (type.equals(Byte.class) || type.equals(byte.class)) {
            return Byte.parseByte(strValue);
        } else if (type.equals(Short.class) || type.equals(short.class)) {
            return Short.parseShort(strValue);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(strValue);
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(strValue);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(strValue);
        } else if (type.equals(Character.class) || type.equals(char.class)) {
            //todo check what to do for longer the 1 char strings
            return strValue.charAt(0);
        }

        return null;
    }
}
