package io.springreact.live;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Marks a class as a server-side "live" component. Its {@code @LiveState} fields hold
 * the UI state (on the server), and its {@code @LiveAction} methods are invoked from the
 * browser over a single WebSocket — no REST endpoints required.
 *
 * <p>It is a Spring stereotype (meta-annotated with {@link Component}), so a live
 * component can constructor-inject any Spring bean (services, repositories, …). A fresh,
 * fully-wired instance is created per mounted component on the client.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface LiveComponent {

    /** The component name the React side mounts via {@code useLive("<name>")}. */
    String value();
}
