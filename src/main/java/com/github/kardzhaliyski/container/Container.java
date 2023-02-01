package com.github.kardzhaliyski.container;

import com.github.kardzhaliyski.container.annotations.*;
import com.github.kardzhaliyski.container.asyncs.AsyncDelegation;
import com.github.kardzhaliyski.container.events.EventListener;
import com.github.kardzhaliyski.container.events.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.*;
import java.util.*;

public class Container {
    private final Map<String, Object> namedInstances = new HashMap<>();
    private final Map<Class<?>, Object> classInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private final Set<Class<?>> initsInProgress = new HashSet<>();
    private ListenerStorage listenerStorage;

    public Container() throws Exception {
            this.listenerStorage = getInstance(ListenerStorage.class);
    }

    public Container(Properties properties) {
        properties.forEach((n, v) -> namedInstances.put(n.toString(), v));
    }

    public Object getInstance(String key) {
        return namedInstances.get(key);
    }

    public <T> T getInstance(Class<T> c) throws Exception {
        Object ins = classInstances.get(c);
        if (ins != null) {
            return (T) ins;
        }

        Class<?> clazzImpl = getClassImpl(c);
        Object instance = classInstances.get(clazzImpl);
        if (instance != null) {
            return (T) instance;
        }

        if (initsInProgress.contains(clazzImpl)) {
            throw new ContainerException("Circular dependency error. For class: " + clazzImpl);
        }

        try {
            initsInProgress.add(clazzImpl);
            ins = (T) initClass(clazzImpl);
        } finally {
            initsInProgress.remove(clazzImpl);
        }

        classInstances.put(c, ins);
        extractListeners(ins);
        return (T) ins;
    }

    private <T> Class<?> getClassImpl(Class<T> c) {
        Class<?> clazzImpl = implementations.get(c);
        if (clazzImpl == null) {
            Default ann = c.getDeclaredAnnotation(Default.class);
            if (ann != null) {
                clazzImpl = ann.value();
                implementations.put(c, clazzImpl);
            }
        }

        if (clazzImpl == null && (c.isInterface() || isAbstract(c))) {
            throw new ContainerException("No implementation found for Interface: " + c.getName());
        }


        clazzImpl = clazzImpl != null ? clazzImpl : c;
        return clazzImpl;
    }

    private void extractListeners(Object ins) throws NoSuchMethodException {
        Class<?> clazz = ins.getClass();
        if (ins instanceof ApplicationListener<?>) {
            Method method = clazz.getMethod("onApplicationEvent", ApplicationEvent.class);
            ListenerInstance li = new ListenerInstance(ins, method);
            listenerStorage.addListener(li);
            return;
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventListener.class)) {
                continue;
            }

            ListenerInstance li = new ListenerInstance(ins, method);
            listenerStorage.addListener(li);
        }
    }

    private <T> boolean isAbstract(Class<T> c) {
        return Modifier.isAbstract(c.getModifiers());
    }

    private Object initClass(Class<?> clazz) throws Exception {
        Object instance = makeInstance(clazz);
        injectFields(instance);

        if (Initializer.class.isAssignableFrom(clazz)) {
            ((Initializer) instance).init();
        }

        classInstances.put(clazz, instance);
        return instance;
    }

    private void injectFields(Object instance) throws Exception {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            Autowire ann = field.getDeclaredAnnotation(Autowire.class);
            if (ann == null) {
                continue;
            }

            field.setAccessible(true);
            Lazy lazy = field.getDeclaredAnnotation(Lazy.class);
            Object o;
            if (lazy != null) {
                o = getLazyObject(instance, field);
                field.set(instance, o);
                continue;
            }

            Qualifier qualifier = field.getDeclaredAnnotation(Qualifier.class);
            if (qualifier != null) {
                o = qualifier.value().isBlank() ? getInstance(field.getName()) : getInstance(qualifier.value());
                field.set(instance, o);
            } else {
                Class<?> type = field.getType();
                if (initsInProgress.contains(type)) {
                    o = getLazyObject(instance, field);
                } else {
                    o = getInstance(type);
                }
            }

            field.set(instance, o);
        }
    }

    private Object getLazyObject(Object instance, Field field) {
        Class<?> type = field.getType();
        return Mockito.mock(type, new Answer() {
            Object inst = instance;
            Field f = field;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Qualifier qualifier = f.getDeclaredAnnotation(Qualifier.class);
                Object o;
                if (qualifier != null) {
                    o = qualifier.value().isBlank() ? getInstance(field.getName()) : qualifier.value();
                } else {
                    o = getInstance(field.getType());
                }

                f.set(inst, o);
                return invocationOnMock.getMethod().invoke(o, invocationOnMock.getArguments());
            }
        });
    }

    private Object makeInstance(Class<?> clazz) throws Exception {
        if (hasAsyncMethods(clazz)) {
            clazz = getAsyncClass(clazz);
        }

        Constructor<?> constructor = getConstructor(clazz);
        Object[] params = getParams(constructor);
        return constructor.newInstance(params);
    }

    private Class<?> getAsyncClass(Class<?> clazz) {
        return new ByteBuddy()
                .subclass(clazz)
                .method(ElementMatchers.isAnnotatedWith(Async.class))
//                .intercept(InvocationHandlerAdapter.of(new AsyncInvocationHandler()))
                .intercept(MethodDelegation.to(AsyncDelegation.class))
                .make()
                .load(clazz.getClassLoader())
                .getLoaded();
    }

    private boolean hasAsyncMethods(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Async.class)) {
                return true;
            }
        }

        return false;
    }


    private Object[] getParams(Constructor<?> constructor) throws Exception {
        Object[] params = null;
        Parameter[] consParams = constructor.getParameters();
        if (consParams.length != 0) {
            params = new Object[consParams.length];
        }

        for (int i = 0; i < consParams.length; i++) {
            Parameter param = consParams[i];
            Qualifier named = param.getAnnotation(Qualifier.class);
            if (named != null) {
                params[i] = getInstance(named.value());
                continue;
            }

            Class<?> pt = param.getType();
            Object o;
//            if (initsInProgress.contains(pt)) {
//                o = getLazyObject(instance, field);
//            } else {
            o = getInstance(pt);
//            }

            params[i] = o;
        }

        return params;
    }

    private static Constructor<?> getConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?> constructor = null;
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            Autowire ann = c.getDeclaredAnnotation(Autowire.class);
            if (ann == null) {
                continue;
            }

            if (constructor != null) {
                throw new ContainerException("More then one constructor set with @Inject annotation. For class: " + clazz.getName());
            }

            c.setAccessible(true);
            constructor = c;
        }

        constructor = constructor != null ? constructor : clazz.getConstructor();
        return constructor;
    }

    public void decorateInstance(Object o) throws Exception {
        injectFields(o);
    }

    public void registerInstance(String key, Object instance) {
        namedInstances.put(key, instance);
    }

    public <T> void registerImplementation(Class<T> c, Class<? extends T> subClass) {
        implementations.put(c, subClass);
    }

    public void registerInstance(Class<?> c, Object instance) {
        if (classInstances.containsKey(c)) {
            throw new IllegalStateException("Instance already exists for class: " + c.getName());
        }

        classInstances.put(c, instance);
    }

    public void registerInstance(Object instance) {
        registerInstance(instance.getClass(), instance);
    }
}
