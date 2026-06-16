package io.springreact.autoconfigure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.util.HtmlUtils;

/**
 * Builds the HTML shell for a React "view". This is the heart of the Thymeleaf-like
 * experience: the server renders the page, selects the view, and inlines the Model so
 * React can render the matching page component with that data as props.
 */
public class ReactRenderer {

    private final ReactProperties properties;
    private final ReactManifest manifest;
    private final ObjectMapper objectMapper;

    public ReactRenderer(ReactProperties properties, ReactManifest manifest, ObjectMapper objectMapper) {
        this.properties = properties;
        this.manifest = manifest;
        this.objectMapper = objectMapper;
    }

    /** Resolve the effective mode, honouring AUTO by probing for a build manifest. */
    public boolean isDev() {
        return switch (properties.getMode()) {
            case DEV -> true;
            case PROD -> false;
            case AUTO -> !manifest.isAvailable();
        };
    }

    public String render(String viewName, Map<String, ?> model) {
        String viewJson = writeJson(viewName);
        String modelJson = writeJson(sanitize(model));

        StringBuilder head = new StringBuilder();
        StringBuilder bodyEnd = new StringBuilder();

        if (isDev()) {
            appendDevTags(head, bodyEnd);
        } else {
            appendProdTags(head, bodyEnd);
        }

        // The Model bridge — inline classic script runs before the deferred module entry,
        // so window.__MODEL__ is guaranteed available when main.tsx executes.
        String bridge = """
                <script>
                  window.__VIEW__ = %s;
                  window.__MODEL__ = %s;
                </script>""".formatted(viewJson, modelJson);

        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1" />
                  <title>%s</title>
                %s
                %s
                </head>
                <body>
                  <div id="root"></div>
                %s
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(properties.getTitle()),
                bridge,
                head.toString(),
                bodyEnd.toString());
    }

    private void appendDevTags(StringBuilder head, StringBuilder bodyEnd) {
        String dev = properties.getDevServerUrl();
        // React Fast Refresh preamble (required by @vitejs/plugin-react in dev).
        head.append("""
                  <script type="module">
                    import RefreshRuntime from "%s/@react-refresh"
                    RefreshRuntime.injectIntoGlobalHook(window)
                    window.$RefreshReg$ = () => {}
                    window.$RefreshSig$ = () => (type) => type
                    window.__vite_plugin_react_preamble_installed__ = true
                  </script>
                  <script type="module" src="%s/@vite/client"></script>
                """.formatted(dev, dev));
        bodyEnd.append("  <script type=\"module\" src=\"%s/%s\"></script>\n"
                .formatted(dev, properties.getEntry()));
    }

    private void appendProdTags(StringBuilder head, StringBuilder bodyEnd) {
        String entry = properties.getEntry();
        String js = manifest.jsFile(entry);
        if (js == null) {
            throw new IllegalStateException("No Vite manifest entry for '" + entry
                    + "'. Did the frontend build run? (mode=PROD requires a manifest)");
        }
        for (String css : manifest.cssFiles(entry)) {
            head.append("  <link rel=\"stylesheet\" href=\"/").append(css).append("\" />\n");
        }
        bodyEnd.append("  <script type=\"module\" src=\"/").append(js).append("\"></script>\n");
    }

    /**
     * Keep only model attributes that are safe/sensible to send to the browser.
     * Drops Spring's internal binding attributes (BindingResult, etc.).
     */
    private Map<String, Object> sanitize(Map<String, ?> model) {
        Map<String, Object> clean = new LinkedHashMap<>();
        if (model != null) {
            for (Map.Entry<String, ?> e : model.entrySet()) {
                if (e.getKey() != null && e.getKey().startsWith("org.springframework")) {
                    continue;
                }
                clean.put(e.getKey(), e.getValue());
            }
        }
        return clean;
    }

    private String writeJson(Object value) {
        try {
            // The output is embedded in a <script>; escape "</" to avoid breaking out of the tag.
            return objectMapper.writeValueAsString(value).replace("</", "<\\/");
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize React model to JSON", ex);
        }
    }

    // Exposed for completeness / testing.
    List<String> cssFor(String entry) {
        return manifest.cssFiles(entry);
    }
}
