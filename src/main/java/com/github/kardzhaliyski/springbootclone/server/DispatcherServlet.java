package com.github.kardzhaliyski.springbootclone.server;

import com.github.kardzhaliyski.springbootclone.annotations.*;
import com.github.kardzhaliyski.springbootclone.context.ApplicationContext;
import com.github.kardzhaliyski.springbootclone.exceptions.ResponseStatusException;
import com.github.kardzhaliyski.springbootclone.interceptors.HandlerInterceptor;
import com.github.kardzhaliyski.springbootclone.interceptors.InterceptorRegistry;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
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

    private final Gson gson;
    private final ApplicationContext applicationContext;
    private final Map<String, RequestHandler> simpleRequests;
    private final Map<Pattern, RequestHandler> complexRequests;
    private final InterceptorRegistry interceptorRegistry;

    public DispatcherServlet(ApplicationContext applicationContext) throws Exception {
        this.applicationContext = applicationContext;
        this.simpleRequests = new HashMap<>();
        this.complexRequests = new HashMap<>();
        this.interceptorRegistry = applicationContext.getInstance(InterceptorRegistry.class);
        this.gson = applicationContext.getInstance(Gson.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HandlerMethod handlerMethod = getHandlerMethod(req);
        if (!preHandleInterceptors(req, resp, handlerMethod)) return;

        Object response = null;
        try {
            RequestHandler requestHandler = handlerMethod.requestHandler;
            Matcher matcher = handlerMethod.matcher;
            response = requestHandler.invoke(req, resp, matcher);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        postHandleInterceptors(req, resp, handlerMethod);
        afterCompletionInterceptors(req, resp, handlerMethod);

        writeResponse(resp, response);
    }

    private void writeResponse(HttpServletResponse resp, Object response) throws IOException {
        PrintWriter writer = resp.getWriter();
        if (!(response instanceof ResponseEntity<?> responseEntity)) {
            writer.println(gson.toJson(response));
            writer.flush();
            return;
        }

        if (responseEntity.headers != null) {
            for (Map.Entry<String, String> header : responseEntity.headers) {
                resp.addHeader(header.getKey(), header.getValue());
            }
        }

        resp.setStatus(responseEntity.status.getCode());
        if (responseEntity.body != null) {
            writer.println(gson.toJson(response));
            writer.flush();
        }
    }


    private boolean preHandleInterceptors(HttpServletRequest req, HttpServletResponse resp, HandlerMethod handlerMethod) throws ServletException {
        for (HandlerInterceptor interceptor : interceptorRegistry.getInterceptors()) {
            try {
                if (!interceptor.preHandle(req, resp, handlerMethod)) {
                    return false;
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        return true;
    }

    private void postHandleInterceptors(HttpServletRequest req, HttpServletResponse resp, HandlerMethod handlerMethod) throws ServletException {
        for (HandlerInterceptor interceptor : interceptorRegistry.getInterceptors()) {
            try {
                interceptor.postHandle(req, resp, handlerMethod, null);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    private void afterCompletionInterceptors(HttpServletRequest req, HttpServletResponse resp, HandlerMethod handlerMethod) throws ServletException {
        for (HandlerInterceptor interceptor : interceptorRegistry.getInterceptors()) {
            try {
                interceptor.afterCompletion(req, resp, handlerMethod, null);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    private HandlerMethod getHandlerMethod(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        String path = normalizePath(req.getMethod(), pathInfo);
        RequestHandler requestHandler = simpleRequests.get(path);
        Matcher matcher = null;
        if (requestHandler == null) {
            matcher = complexRequestPattern(path);
        }

        if (matcher != null) {
            requestHandler = complexRequests.get(matcher.pattern());
        }

        if (requestHandler == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        HandlerMethod handlerMethod = new HandlerMethod(requestHandler, matcher);
        return handlerMethod;
    }

    private Matcher complexRequestPattern(String path) {
        for (Map.Entry<Pattern, RequestHandler> kvp : complexRequests.entrySet()) {
            Pattern pattern = kvp.getKey();
            Matcher m = pattern.matcher(path);
            if (m.matches()) {
                return m;
            }
        }

        return null;
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
