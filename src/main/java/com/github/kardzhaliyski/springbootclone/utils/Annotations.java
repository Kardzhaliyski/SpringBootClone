package com.github.kardzhaliyski.springbootclone.utils;

import com.github.kardzhaliyski.springbootclone.annotations.Component;

import java.lang.annotation.Annotation;
import java.util.*;

@Component
public class Annotations {
    private Map<Class<?>, Set<Annotation>> annotationMap = new HashMap<>();
    private Map<Class<?>, Set<Class<? extends Annotation>>> classMap = new HashMap<>();

    public boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        if (!annotationMap.containsKey(clazz)) {
            extractAnnotations(clazz);
        }

        return classMap.get(clazz).contains(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType) {
        if (!isAnnotationPresent(clazz, annotationType)) {
            return null;
        }

        for (Annotation ann : annotationMap.get(clazz)) {
            if (ann.annotationType().equals(annotationType)) {
                return (T) ann;
            }
        }

        return null;
    }

    private void extractAnnotations(Class<?> clazz) {
        Set<Annotation> annotations = new HashSet<>();
        Set<Class<? extends Annotation>> types = new HashSet<>();
        for (Annotation ann : clazz.getAnnotations()) {
            addIfMissing(ann, annotations, types);
        }

        for (Class<?> anInterface : clazz.getInterfaces()) {
            for (Annotation ann : anInterface.getAnnotations()) {
                addIfMissing(ann, annotations, types);
            }
        }

        Set<Annotation> innerAnnotations = new HashSet<>();
        for (Annotation ann : annotations) {
            Collections.addAll(innerAnnotations, ann.annotationType().getAnnotations());
        }

        for (Annotation ann : innerAnnotations) {
            addIfMissing(ann, annotations, types);
        }

        annotationMap.put(clazz, annotations);
        classMap.put(clazz, types);
    }

    private static void addIfMissing(Annotation ann, Set<Annotation> annotations, Set<Class<? extends Annotation>> types) {
        Class<? extends Annotation> type = ann.annotationType();
        if (types.contains(type)) {
            return;
        }

        types.add(type);
        annotations.add(ann);
    }

}
