package com.github.kardzhaliyski.springbootclone.server;

import com.github.kardzhaliyski.springbootclone.annotations.*;
import com.github.kardzhaliyski.springbootclone.context.ApplicationContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {
    private static final Set<Class<?>> REQUEST_ANNOTATIONS = Set.of(
            GetMapping.class,
            PutMapping.class,
            PostMapping.class,
            DeleteMapping.class);

    private final ApplicationContext applicationContext;
    Map<String, RequestHandler> simpleRequests;
    Map<Pattern, RequestHandler> complexRequests;

    public DispatcherServlet(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.simpleRequests = new HashMap<>();
        this.complexRequests = new HashMap<>();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        String method = req.getMethod();
        String path = normalizePath(method, pathInfo);

        RequestHandler requestHandler = simpleRequests.get(path);
        if (requestHandler != null) {
            try {
                requestHandler.invoke(req, resp);
            } catch (Exception e) {
                throw new RuntimeException(e); //todo
            }
        } else {
            for (Map.Entry<Pattern, RequestHandler> kvp : complexRequests.entrySet()) {
                Pattern pattern = kvp.getKey();
                Matcher matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    requestHandler = kvp.getValue();
                    try {
                        requestHandler.invoke(req, resp, matcher);
                    } catch (Exception e) {
                        throw new RuntimeException(e);//todo
                    }
                    break;
                }
            }
        }

        if (requestHandler == null) {
            return;
            //todo return 404
        }
    }

    public ApplicationContext getContainer() {
        return applicationContext;
    }

    public void addController(Object controller) {//todo need refactoring
        Class<?> clazz = controller.getClass();
        if (!clazz.isAnnotationPresent(Controller.class) && !clazz.isAnnotationPresent(RestController.class)) {
            throw new IllegalArgumentException("Class should be annotated with Controller or RestController annotation. For class: " + clazz.getName());
        }

        RequestMapping ann = clazz.getAnnotation(RequestMapping.class);
        String pathPrefix = ann != null ? ann.value() : "";

        for (Method method : clazz.getMethods()) {
            Annotation mAnn = getMappingAnnotation(method);
            if (mAnn == null) {
                continue;
            }

            String methodType = "";
            String[] paths = switch (mAnn) {
                case GetMapping a -> {
                    methodType = "GET";
                    yield a.value();
                }
                case PostMapping a -> {
                    methodType = "POST";
                    yield a.value();
                }

                case PutMapping a -> {
                    methodType = "PUT";
                    yield a.value();
                }
                case DeleteMapping a -> {
                    methodType = "DELETE";
                    yield a.value();
                }
                default -> new String[0];// shouldn't happen
            };

            for (String p : paths) {
                String path = normalizePath(methodType, pathPrefix, p);
                RequestHandler requestHandler = new RequestHandler(controller, method, this);

                if (path.contains("*") || path.contains("{")) {
                    Pattern pattern = compilePattern(path);
                    complexRequests.put(pattern, requestHandler);
                } else {
                    simpleRequests.put(path, requestHandler);
                }
            }
        }
    }

    private Annotation getMappingAnnotation(Method method) {
        for (Annotation ann : method.getAnnotations()) {
            if (REQUEST_ANNOTATIONS.contains(ann.annotationType())) {
                return ann;
            }
        }

        return null;
    }

    private String normalizePath(String methodType, String path) {
        String newPath = methodType.toUpperCase() + ":" + path;
        if (newPath.length() > 1 && newPath.endsWith("/")) {
            newPath = newPath.substring(0, newPath.length() - 2);
        }

        return newPath;
    }

    private String normalizePath(String methodType, String pathPrefix, String path) {
        if (path.equals("") || path.equals("/")) {
            return normalizePath(methodType, pathPrefix);
        }

        if (pathPrefix.endsWith("/") && path.startsWith("/")) {
            path = path.substring(1);
        } else if (!pathPrefix.endsWith("/") && !path.startsWith("/")) {
            path = "/" + path;
        }

        if (pathPrefix.length() > 0 && !pathPrefix.startsWith("/")) {
            pathPrefix = "/" + pathPrefix;
        }

        path = pathPrefix + path;
        return normalizePath(methodType, path);
    }

    private Pattern compilePattern(String path) {
        path = path.replaceAll("\\.", "\\\\.");
        path = path.replaceAll("\\*", "\\.+");
        path = path.replaceAll("\\{\\w+}", "([\\\\w-\\\\.]+)");
        return Pattern.compile(path);
    }
}
