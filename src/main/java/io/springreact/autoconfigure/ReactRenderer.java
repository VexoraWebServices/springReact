package io.springreact.autoconfigure;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.util.HtmlUtils;

/**
 * Renders the HTML shell for a screen. The shell inlines the chosen view name and the
 * controller's model, then loads the framework's bundled runtime — which reads them and
 * mounts the matching Java/Kotlin Server Component. No consumer frontend involved.
 */
public class ReactRenderer {

    private final ReactProperties properties;
    private final ObjectMapper objectMapper;

    public ReactRenderer(ReactProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String render(String viewName, Map<String, ?> model) {
        String viewJson = writeJson(viewName);
        String modelJson = writeJson(sanitize(model));
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1" />
                  <title>%s</title>
                  <script>
                    window.__VIEW__ = %s;
                    window.__MODEL__ = %s;
                  </script>
                </head>
                <body>
                  <div id="root"></div>
                  <script src="%s"></script>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(properties.getTitle()),
                viewJson,
                modelJson,
                HtmlUtils.htmlEscape(properties.getRuntimePath()));
    }

    /** Drop Spring's internal binding attributes before sending the model to the browser. */
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
            // Embedded in a <script>; escape "</" so the value can't break out of the tag.
            return objectMapper.writeValueAsString(value).replace("</", "<\\/");
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize view/model to JSON", ex);
        }
    }
}
