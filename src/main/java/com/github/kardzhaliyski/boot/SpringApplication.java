package com.github.kardzhaliyski.boot;

import com.github.kardzhaliyski.container.Container;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringApplication {
    private final Container container;

    public SpringApplication() throws Exception {
        container = new Container();
    }

    public static void run(Class<?> primaryClass, String... args) throws URISyntaxException, IOException {
        Set<Class<?>> classes = getClasses(primaryClass);
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

    public static void main(String[] args) throws Exception {
        SpringApplication springApplication = new SpringApplication();
        springApplication.run(Container.class);
    }
}
