package com.github.kardzhaliyski.server;

import com.github.kardzhaliyski.boot.annotations.PathVariable;
import com.github.kardzhaliyski.boot.annotations.RequestBody;
import com.github.kardzhaliyski.boot.utils.HttpHeaders;
import com.github.kardzhaliyski.container.annotations.Qualifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.regex.Matcher;

public class RequestHandler {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
            try {
                response = method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e); //todo
            }
        } else {
            Object[] params = new Object[paramTypes.length];
            int pathVarIndex = 1;
            for (int i = 0; i < paramTypes.length; i++) {
                Parameter paramType = paramTypes[i];
                if (paramType.isAnnotationPresent(RequestBody.class)) {
                    String contentType = req.getHeader(HttpHeaders.CONTENT_TYPE);
                    if (!contentType.equalsIgnoreCase("application/json")) {
                        //todo return not supported media type
                        throw new IllegalArgumentException();//todo remove
                    }

                    params[i] = gson.fromJson(req.getReader(), paramType.getType());
                } else if (paramType.isAnnotationPresent(PathVariable.class)) {
                    if (matcher == null || matcher.groupCount() == 0) {
                        throw new IllegalArgumentException(); //todo return bad request
                    }

                    params[i] = getPathVariable(paramType.getType(), matcher, pathVarIndex);

                } else if (paramType.isAnnotationPresent(Qualifier.class)) {
                    String qualifier = paramType.getAnnotation(Qualifier.class).value();
                    params[i] = dispatcherServlet.getContainer().getInstance(qualifier);
                } else {
                    params[i] = dispatcherServlet.getContainer().getInstance(paramType.getType());
                }
            }

            try {
                response = method.invoke(instance, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);//todo
            }
        }


        PrintWriter writer = resp.getWriter();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();//todo remove from here
        writer.println(gson.toJson(response));
        writer.flush();
    }

    private Object getPathVariable(Class<?> type, Matcher matcher, int pathVarIndex) {
        String strValue = matcher.group(pathVarIndex);
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
