package com.github.kardzhaliyski.boot;

import com.github.kardzhaliyski.boot.annotations.Bean;
import com.github.kardzhaliyski.boot.annotations.Configuration;
import com.github.kardzhaliyski.boot.annotations.Mapper;
import com.github.kardzhaliyski.container.Container;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringApplication {
    private static Container container;


    private static void init() {
        try {
            container = new Container();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void run(Class<?> primaryClass, String... args) throws Exception {
        init();
        Set<Class<?>> classes = getClasses(primaryClass);
        extractInterfaceImplementations(classes);
        extractBeansFromConfigurations(classes);
        for (Class<?> clazz : classes) {
            if(!clazz.isInterface() || !clazz.isAnnotationPresent(Mapper.class)) {
                continue;
            }

        }




        initClasses(classes);
    }

    private static void initClasses(Set<Class<?>> classes) throws Exception {
        for (Class<?> clazz : classes) {
            if(clazz.isInterface()){
                continue;
            }

            container.getInstance(clazz);
        }
    }

    private static void extractBeansFromConfigurations(Set<Class<?>> classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }

            if (!clazz.isAnnotationPresent(Configuration.class)) {
                continue;
            }

            Object instance = container.getInstance(clazz);

            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(Bean.class)) {
                    continue;
                }

                if (method.getParameterCount() == 0) {
                    method.invoke(instance);
                    continue;
                }

                Class<?>[] paramTypes = method.getParameterTypes();
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    Object paramInst = container.getInstance(paramTypes[i]);
                    params[i] = paramInst;
                }

                Object obj = method.invoke(instance, params);
                container.registerInstance(obj);
            }
        }
    }

    private static void extractInterfaceImplementations(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }

            for (Class<?> anInterface : clazz.getInterfaces()) {
                container.registerImplementation(anInterface, clazz);
            }
        }
    }

    private static Set<Class<?>> getClasses(Class<?> primaryClass) throws URISyntaxException, IOException {
        Package aPackage = primaryClass.getPackage();
        Path directory = Path.of(primaryClass.getResource("").toURI());
        Set<Class<?>> classes = Files.walk(directory)
                .filter((f) -> f.toString().endsWith(".class"))
                .filter((f) -> !f.toString().contains("$"))
                .map(SpringApplication::getClassPath)
                .map(SpringApplication::getClass)
                .collect(Collectors.toSet());
        return classes;
    }

    private static String getClassPath(Path f) {
        String stringPath = f.toString();
        int index = stringPath.indexOf("com\\github\\kardzhaliyski");
        stringPath = stringPath.substring(index);
        stringPath = stringPath.replaceAll("\\\\", ".");
        return stringPath.substring(0, stringPath.length() - ".class".length());
    }


    private static Class<?> getClass(String classPath)  {
        try {
            return Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
