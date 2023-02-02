package com.github.kardzhaliyski.boot;

import com.github.kardzhaliyski.boot.annotations.*;
import com.github.kardzhaliyski.boot.utils.MybatisConfig;
import com.github.kardzhaliyski.container.Container;
import org.apache.ibatis.io.Resources;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ContainerAutoConfigurator {
    public final Set<Class<?>> COMPONENT_ANNOTATIONS = Set.of(
            Component.class,
            Controller.class,
            RestController.class,
            Service.class,
            Mapper.class);
    private final Container container;

    public ContainerAutoConfigurator() {
        try {
            container = new Container();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ContainerAutoConfigurator(Container container) {
        this.container = container;
    }

    public void run(Class<?> primaryClass) throws Exception {
        Set<Class<?>> classes = getClasses(primaryClass);
        Properties properties = Resources.getResourceAsProperties("application.properties");
        properties.forEach((k, v) -> container.registerInstance((String) k, v));
        extractInterfaceImplementations(classes);
        extractBeansFromConfigurations(classes);
        MybatisConfig.init(container, classes);
        initComponents(classes);
    }

    private void initComponents(Set<Class<?>> classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || clazz.isEnum()) {
                continue;
            }

            if (!isComponent(clazz)) {
                continue;
            }

            container.getInstance(clazz);
        }
    }

    private boolean isComponent(Class<?> clazz) {
        for (Annotation annotation : clazz.getAnnotations()) {
            if (COMPONENT_ANNOTATIONS.contains(annotation.getClass())){
                return true;
            }
        }

        return false;
    }

    private void extractBeansFromConfigurations(Set<Class<?>> classes) throws Exception {
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

                Object obj;
                if (method.getParameterCount() == 0) {
                    obj = method.invoke(instance);
                } else {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        Object paramInst = container.getInstance(paramTypes[i]);
                        params[i] = paramInst;
                    }

                    obj = method.invoke(instance, params);
                }

                Class<?> returnType = method.getReturnType();
                container.registerInstance(returnType, obj);
            }
        }
    }

    private void extractInterfaceImplementations(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }

            for (Class<?> anInterface : clazz.getInterfaces()) {
                container.registerImplementation(anInterface, clazz);
            }
        }
    }

    private Set<Class<?>> getClasses(Class<?> primaryClass) throws URISyntaxException, IOException {
        Path directory = Path.of(primaryClass.getResource("").toURI());
        return Files.walk(directory)
                .filter((f) -> f.toString().endsWith(".class"))
                .filter((f) -> !f.toString().contains("$"))
                .map(this::getClassPath)
                .map(this::getClass)
                .collect(Collectors.toSet());
    }

    private String getClassPath(Path f) {
        String stringPath = f.toString();
        int index = stringPath.indexOf("com\\github\\kardzhaliyski");
        stringPath = stringPath.substring(index);
        stringPath = stringPath.replaceAll("\\\\", ".");
        return stringPath.substring(0, stringPath.length() - ".class".length());
    }


    private Class<?> getClass(String classPath) {
        try {
            return Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}