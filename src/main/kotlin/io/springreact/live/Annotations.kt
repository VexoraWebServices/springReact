package io.springreact.live

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Marks a class as a server-side live/server component. A fresh, fully dependency-injected
 * instance is created per mounted client component (prototype `@Component`).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
annotation class LiveComponent(val value: String)

/** Marks a field as part of a component's serialized state (the only thing sent to the client). */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class LiveState

/** Marks a function as callable from the browser via `call("name", ...args)`. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LiveAction(val value: String = "")
