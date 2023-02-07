package com.github.kardzhaliyski.springbootclone;

import com.github.kardzhaliyski.springbootclone.annotations.*;
import com.github.kardzhaliyski.springbootclone.interceptors.InterceptorRegistry;
import com.github.kardzhaliyski.springbootclone.interceptors.WebMvcConfigurer;
import com.github.kardzhaliyski.springbootclone.utils.MybatisConfig;
import com.github.kardzhaliyski.springbootclone.context.ApplicationContext;
import com.github.kardzhaliyski.springbootclone.server.DispatcherServlet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.io.Resources;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ContainerAutoConfigurator {
    public final Set<Class<?>> COMPONENT_ANNOTATIONS = Set.of(
            Component.class,
            Controller.class,
            RestController.class,
            Service.class,
            Mapper.class);

    private final ApplicationContext applicationContext;
    private DispatcherServlet dispatcherServlet;
    private MybatisConfig mybatisConfig;

    public ContainerAutoConfigurator() throws Exception {
        applicationContext = new ApplicationContext();
        init();
    }

    public ContainerAutoConfigurator(ApplicationContext applicationContext) throws Exception {
        this.applicationContext = applicationContext;
        init();
    }

    private void init() throws Exception {
        importProperties();
        mybatisConfig = new MybatisConfig(applicationContext);
        mybatisConfig.init();
        applicationContext.registerInstance(MybatisConfig.class, mybatisConfig);
        dispatcherServlet = applicationContext.getInstance(DispatcherServlet.class);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private void scanPackages(Class<?>... baseClasses) throws Exception {
        for (Class<?> clazz : baseClasses) {
            scanPackage(clazz);
        }
    }

    public void scanPackage(Class<?> baseClass) throws Exception {
        Set<Class<?>> classes = scanClasses(baseClass);
        extractInterfaceImplementations(classes);
        mybatisConfig.initMappers(classes.toArray(Class[]::new));
        extractBeansFromConfigurations(classes);
        initComponents(classes);
    }

    private void importProperties() throws IOException {
        Properties properties = Resources.getResourceAsProperties("application.properties");
        properties.forEach((k, v) -> applicationContext.registerInstance((String) k, v));
    }

    private void initComponents(Set<Class<?>> classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || clazz.isEnum()) {
                continue;
            }

            if (!isComponent(clazz)) {
                continue;
            }

            Object instance = applicationContext.getInstance(clazz);
            if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                dispatcherServlet.addController(instance);
            }
        }
    }

    private boolean isComponent(Class<?> clazz) {
        for (Annotation annotation : clazz.getAnnotations()) {
            if (COMPONENT_ANNOTATIONS.contains(annotation.annotationType())) {
                return true;
            }
        }

        return false;
    }

    private void extractBeansFromConfigurations(Set<Class<?>> classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || !clazz.isAnnotationPresent(Configuration.class)) {
                continue;
            }

            if (clazz.isAnnotationPresent(ComponentScan.class)) {
                ComponentScan cs = clazz.getAnnotation(ComponentScan.class);
                if (cs.basePackageClasses().length != 0) {
                    this.scanPackages(cs.basePackageClasses());
                } else {
                    this.scanPackage(clazz);
                }
            }

            Object instance = applicationContext.getInstance(clazz);
            if(instance instanceof WebMvcConfigurer webMvcConfigurer) {
                InterceptorRegistry registry = applicationContext.getInstance(InterceptorRegistry.class);
                webMvcConfigurer.addInterceptors(registry);
            }

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
                        Object paramInst = applicationContext.getInstance(paramTypes[i]);
                        params[i] = paramInst;
                    }

                    obj = method.invoke(instance, params);
                }

                Class<?> returnType = method.getReturnType();
                applicationContext.registerInstance(returnType, obj);
            }
        }
    }

    private void extractInterfaceImplementations(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }

            for (Class<?> anInterface : clazz.getInterfaces()) {
                applicationContext.registerImplementation(anInterface, clazz);
            }
        }
    }

    private Set<Class<?>> scanClasses(Class<?> primaryClass) throws URISyntaxException, IOException {
        File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile()) {
            return scanClassesFromJar(primaryClass, jarFile);
        } else {
            //For IDE use
            Path directory = Path.of(primaryClass.getResource("").toURI());
            return Files.walk(directory)
                    .filter((f) -> f.toString().endsWith(".class"))
                    .filter((f) -> !f.toString().contains("$"))
                    .map(this::getClassPath)
                    .map(this::getClass)
                    .collect(Collectors.toSet());
        }
    }

    private Set<Class<?>> scanClassesFromJar(Class<?> primaryClass, File jarFile) throws IOException {
        String path = primaryClass.getPackage().getName();
        path = path.replaceAll("\\.", "/");
        try (JarFile jar = new JarFile(jarFile)) {
            Set<Class<?>> set = new HashSet<>();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (jarEntry.isDirectory()
                        || !name.startsWith(path)
                        || !name.endsWith(".class")
                        || name.contains("$")) {
                    continue;
                }

                name = name.replaceAll("/", ".");
                name = name.substring(0, name.length() - ".class".length());
                Class<?> clazz = getClass(name);
                set.add(clazz);
            }

            return set;
        }
    }

    private String getClassPath(Path f) {
        String stringPath = f.toString();
        int index = stringPath.indexOf("classes");
        stringPath = stringPath.substring(index + "classes\\".length());
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
