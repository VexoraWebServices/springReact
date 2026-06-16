package io.springreact.live

import org.springframework.context.ApplicationContext
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/** Reflective metadata for one component class: its `@LiveAction`s and `@LiveState` fields. */
class LiveComponentDescriptor private constructor() {

    private val actions = HashMap<String, Method>()
    private val stateFields = ArrayList<Field>()
    private val paramFields = ArrayList<Pair<String, Field>>()

    fun action(name: String): Method? = actions[name]

    /** Route-param name → field, for `@LiveParam` fields. */
    fun paramFields(): List<Pair<String, Field>> = paramFields

    fun readState(instance: Any): Map<String, Any?> {
        val state = LinkedHashMap<String, Any?>()
        for (field in stateFields) state[field.name] = ReflectionUtils.getField(field, instance)
        return state
    }

    companion object {
        fun of(type: Class<*>): LiveComponentDescriptor {
            val d = LiveComponentDescriptor()
            ReflectionUtils.doWithMethods(type) { method ->
                method.getAnnotation(LiveAction::class.java)?.let { a ->
                    val name = a.value.ifEmpty { method.name }
                    method.isAccessible = true
                    d.actions[name] = method
                }
            }
            ReflectionUtils.doWithFields(type) { field ->
                if (field.isAnnotationPresent(LiveState::class.java)) {
                    field.isAccessible = true
                    d.stateFields.add(field)
                }
                field.getAnnotation(LiveParam::class.java)?.let { a ->
                    field.isAccessible = true
                    d.paramFields.add((a.value.ifEmpty { field.name }) to field)
                }
            }
            return d
        }
    }
}

/**
 * Discovers all `@LiveComponent` beans at startup and creates fresh, fully injected
 * instances on demand (one per mounted client component).
 */
class LiveComponentRegistry(private val context: ApplicationContext) {

    private val componentsByName = HashMap<String, Class<*>>()
    private val descriptors = ConcurrentHashMap<Class<*>, LiveComponentDescriptor>()

    init {
        for (beanName in context.getBeanNamesForAnnotation(LiveComponent::class.java)) {
            val type = context.getType(beanName)
            val annotation = context.findAnnotationOnBean(beanName, LiveComponent::class.java)
            if (type != null && annotation != null) {
                componentsByName[annotation.value] = ClassUtils.getUserClass(type)
            }
        }
    }

    fun has(name: String) = componentsByName.containsKey(name)

    fun create(name: String): Any {
        val type = componentsByName[name] ?: throw IllegalArgumentException("Unknown live component: '$name'")
        // Always a fresh, fully-wired instance regardless of declared scope.
        return context.autowireCapableBeanFactory.createBean(type)
    }

    fun descriptor(instance: Any): LiveComponentDescriptor =
        descriptors.computeIfAbsent(ClassUtils.getUserClass(instance)) { LiveComponentDescriptor.of(it) }
}
