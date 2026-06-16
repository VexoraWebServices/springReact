package io.springreact.live;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * Discovers all {@link LiveComponent}-annotated beans at startup and creates fresh,
 * fully dependency-injected instances on demand (one per mounted client component).
 */
public class LiveComponentRegistry {

    private final ApplicationContext context;
    private final Map<String, Class<?>> componentsByName = new HashMap<>();
    private final Map<Class<?>, LiveComponentDescriptor> descriptors = new ConcurrentHashMap<>();

    public LiveComponentRegistry(ApplicationContext context) {
        this.context = context;
        for (String beanName : context.getBeanNamesForAnnotation(LiveComponent.class)) {
            Class<?> type = context.getType(beanName);
            LiveComponent annotation = context.findAnnotationOnBean(beanName, LiveComponent.class);
            if (type != null && annotation != null) {
                componentsByName.put(annotation.value(), ClassUtils.getUserClass(type));
            }
        }
    }

    public boolean has(String name) {
        return componentsByName.containsKey(name);
    }

    public Set<String> names() {
        return componentsByName.keySet();
    }

    /** Create a new, autowired instance of the named component. */
    public Object create(String name) {
        Class<?> type = componentsByName.get(name);
        if (type == null) {
            throw new IllegalArgumentException("Unknown live component: '" + name + "'");
        }
        // createBean() always returns a fresh instance with full constructor/field
        // injection, regardless of the bean's declared scope.
        return context.getAutowireCapableBeanFactory().createBean(type);
    }

    public LiveComponentDescriptor descriptor(Object instance) {
        return descriptors.computeIfAbsent(ClassUtils.getUserClass(instance),
                LiveComponentDescriptor::of);
    }
}
