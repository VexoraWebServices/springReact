package io.springreact.live;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as part of a live component's serialized state. After every action the
 * server sends the current values of all {@code @LiveState} fields to the browser, where
 * they become the component's props. Fields without this annotation (e.g. injected
 * services) are never sent to the client.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LiveState {
}
