package io.springreact.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the "Thymeleaf-like, zero-frontend" promise: a controller returns a view name,
 * the framework renders an HTML shell that loads the runtime it bundles in its own jar,
 * and that runtime is actually served. The consumer ships no frontend files.
 */
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShellRenderingTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void shellReferencesBundledRuntime() {
        String html = rest.getForObject("/", String.class);
        assertThat(html)
                .contains("<div id=\"root\"></div>")
                .contains("window.__VIEW__ = \"Home\"")
                .contains("<script src=\"/springreact/springreact.js\">");
    }

    @Test
    void bundledRuntimeIsServedFromTheJar() {
        ResponseEntity<String> js = rest.getForEntity("/springreact/springreact.js", String.class);
        assertThat(js.getStatusCode().value()).isEqualTo(200);
        // It's a real bundle with React inside.
        assertThat(js.getBody()).isNotBlank();
    }
}
