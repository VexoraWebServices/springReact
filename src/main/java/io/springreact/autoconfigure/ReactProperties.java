package io.springreact.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for SpringReact, bound from {@code spring.react.*}. Zero config is
 * required: the framework bundles its own React runtime and serves it.
 */
@ConfigurationProperties(prefix = "spring.react")
public class ReactProperties {

    /** Default {@code <title>} for the rendered HTML shell. */
    private String title = "SpringReact";

    /** URL of the bundled runtime script (served from the jar by default). */
    private String runtimePath = "/springreact/springreact.js";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRuntimePath() {
        return runtimePath;
    }

    public void setRuntimePath(String runtimePath) {
        this.runtimePath = runtimePath;
    }
}
