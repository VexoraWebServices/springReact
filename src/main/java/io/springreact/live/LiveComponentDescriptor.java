package io.springreact.live;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.util.ReflectionUtils;

/**
 * Reflective metadata for one live component class: its {@code @LiveAction} methods and
 * {@code @LiveState} fields. Computed once per class and cached by the registry.
 */
public class LiveComponentDescriptor {

    private final Map<String, Method> actions = new HashMap<>();
    private final List<Field> stateFields = new CopyOnWriteArrayList<>();

    public static LiveComponentDescriptor of(Class<?> type) {
        LiveComponentDescriptor d = new LiveComponentDescriptor();
        ReflectionUtils.doWithMethods(type, method -> {
            LiveAction a = method.getAnnotation(LiveAction.class);
            if (a != null) {
                String name = a.value().isEmpty() ? method.getName() : a.value();
                method.setAccessible(true);
                d.actions.put(name, method);
            }
        });
        ReflectionUtils.doWithFields(type, field -> {
            if (field.isAnnotationPresent(LiveState.class)) {
                field.setAccessible(true);
                d.stateFields.add(field);
            }
        });
        return d;
    }

    public Method action(String name) {
        return actions.get(name);
    }

    /** Snapshot the current {@code @LiveState} field values for serialization. */
    public Map<String, Object> readState(Object instance) {
        Map<String, Object> state = new LinkedHashMap<>();
        for (Field field : stateFields) {
            state.put(field.getName(), ReflectionUtils.getField(field, instance));
        }
        return state;
    }
}
