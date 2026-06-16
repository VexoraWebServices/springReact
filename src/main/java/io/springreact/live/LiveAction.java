package io.springreact.live;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as callable from the browser via {@code call("<name>", ...args)}.
 * Method parameters are deserialized from the client's argument array. After the method
 * runs, the component's updated {@code @LiveState} is streamed back automatically.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LiveAction {

    /** Action name. Defaults to the method name when empty. */
    String value() default "";
}
