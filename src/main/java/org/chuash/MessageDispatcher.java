package org.chuash;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

@Component
public class MessageDispatcher {

    private final ClassPathControllerScanner classPathControllerScanner;

    public MessageDispatcher(ClassPathControllerScanner classPathControllerScanner) {
        this.classPathControllerScanner = classPathControllerScanner;
    }

    public void dispatch(@NotNull Update update) {
        if (update.hasCallbackQuery()) {
            String callbackQuery = update.getCallbackQuery().getData();
            dispatchToHandler(callbackQuery, update,
                    classPathControllerScanner.getCallBackQueryMappingHolder(),
                    classPathControllerScanner.getRegexCallBackQueryMappingHolder());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            dispatchToHandler(message, update,
                    classPathControllerScanner.getMessageMappingHolder(),
                    classPathControllerScanner.getRegexMessageMappingHolder());
        }
    }

    private void dispatchToHandler(String input, Update update,
                                   Map<String, Pair<Object, Method>> mappingHolder,
                                   Map<String, Pair<Object, Method>> regexMappingHolder) {
        Optional.ofNullable(mappingHolder.get(input))
                .ifPresent(pair -> invokeMethod(pair, update));

        regexMappingHolder.entrySet().stream()
                .filter(entry -> input.matches(entry.getKey()))
                .forEach(entry -> invokeMethod(entry.getValue(), update));
    }

    private void invokeMethod(Pair<Object, Method> pair, Update update) {
        try {
            pair.getRight().invoke(pair.getLeft(), update);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method", e);
        }
    }
}
