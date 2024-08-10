package org.chuash;

import org.apache.commons.lang3.tuple.Pair;
import org.chuash.annotation.CallBackQueryMapping;
import org.chuash.annotation.MessageMapping;
import org.chuash.annotation.UpdateController;
import org.chuash.exception.MethodParameterException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ClassPathControllerScanner {

    private final Map<String, Pair<Object, Method>> messageMappingHolder = new HashMap<>();
    private final Map<String, Pair<Object, Method>> regexMessageMappingHolder = new HashMap<>();
    private final Map<String, Pair<Object, Method>> callBackQueryMappingHolder = new HashMap<>();
    private final Map<String, Pair<Object, Method>> regexCallBackQueryMappingHolder = new HashMap<>();

    private final ApplicationContext applicationContext;
    public ClassPathControllerScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void findCandidates() {
        Arrays.stream(applicationContext.getBeanNamesForAnnotation(UpdateController.class))
                .map(applicationContext::getBean)
                .forEach(this::processInstance);
    }

    private void processInstance(Object instance) {
        Class<?> clazz = instance.getClass();
        Arrays.stream(clazz.getMethods())
                .forEach(method -> {
                    if (isAnnotatedWith(method, MessageMapping.class)) {
                        processMessageMapping(method, instance);
                    } else if (isAnnotatedWith(method, CallBackQueryMapping.class)) {
                        processCallbackQueryMapping(method, instance);
                    }
                });
        boolean is = clazz.isAnnotationPresent(MessageMapping.class);
    }

    private boolean isAnnotatedWith(Method method, Class<? extends Annotation> annotationClass) {
        return method.isAnnotationPresent(annotationClass);
    }

    private void processMessageMapping(Method method, Object instance) {
        validateMethod(method);
        MessageMapping annotation = method.getAnnotation(MessageMapping.class);
        String key = annotation.value();
        if (annotation.isRegex()) {
            regexMessageMappingHolder.put(key, Pair.of(instance, method));
        } else {
            messageMappingHolder.put(key, Pair.of(instance, method));
        }
    }

    private void processCallbackQueryMapping(Method method, Object instance) {
        validateMethod(method);
        CallBackQueryMapping annotation = method.getAnnotation(CallBackQueryMapping.class);
        String key = annotation.value();
        if (annotation.isRegex()) {
            regexCallBackQueryMappingHolder.put(key, Pair.of(instance, method));
        } else {
            callBackQueryMappingHolder.put(key, Pair.of(instance, method));
        }
    }

    private void validateMethod(Method method) {
        if (method.getParameterCount() == 0 || !Arrays.asList(method.getParameterTypes()).contains(Update.class)) {
            throw new MethodParameterException(String.format(
                    "Method [%s] must have at least one parameter of type [org.telegram.telegrambots.meta.api.objects.Update]",
                    method));
        }
    }

    public Map<String, Pair<Object, Method>> getRegexCallBackQueryMappingHolder() {
        return regexCallBackQueryMappingHolder;
    }

    public Map<String, Pair<Object, Method>> getCallBackQueryMappingHolder() {
        return callBackQueryMappingHolder;
    }

    public Map<String, Pair<Object, Method>> getRegexMessageMappingHolder() {
        return regexMessageMappingHolder;
    }

    public Map<String, Pair<Object, Method>> getMessageMappingHolder() {
        return messageMappingHolder;
    }
}
