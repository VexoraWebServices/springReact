package com.vexora.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

/**
 * Consumer assets: `spring.react.stylesheets` and `spring.react.scripts` inject `<link>`
 * and `<script>` into the shell — the hooks for Tailwind/CSS and custom-widget bundles
 * (charts, three.js, …).
 */
@SpringBootTest(
    classes = [TestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.react.stylesheets=/app.css,https://cdn.example.com/x.css",
        "spring.react.scripts=/widgets.js",
    ],
)
class CustomAssetsTest {

    @Autowired
    lateinit var rest: TestRestTemplate

    @Test
    fun stylesheetsAndScriptsAreInjected() {
        val html = rest.getForObject("/", String::class.java)
        assertThat(html)
            .contains("""<link rel="stylesheet" href="/app.css" />""")
            .contains("""<link rel="stylesheet" href="https://cdn.example.com/x.css" />""")
            // widget bundle loads AFTER the runtime so window.SpringReact exists
            .contains("""<script src="/widgets.js"></script>""")
        val runtimeIdx = html!!.indexOf("springreact/springreact.js")
        val widgetIdx = html.indexOf("/widgets.js")
        assertThat(widgetIdx).isGreaterThan(runtimeIdx)
    }
}
