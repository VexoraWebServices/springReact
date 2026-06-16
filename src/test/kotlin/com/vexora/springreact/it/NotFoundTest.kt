package com.vexora.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

/**
 * An unknown URL renders the configured not-found component (with a 404 status) instead of
 * the Spring whitelabel page.
 */
@SpringBootTest(
    classes = [TestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.react.not-found-view=NotFound"],
)
class NotFoundTest {

    @Autowired
    lateinit var rest: TestRestTemplate

    @Test
    fun unknownUrlRendersTheNotFoundComponent() {
        // Browsers send Accept: text/html — that's what triggers the HTML error view.
        val headers = HttpHeaders().apply { accept = listOf(MediaType.TEXT_HTML) }
        val response = rest.exchange(
            "/this-page-does-not-exist",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            String::class.java,
        )
        assertThat(response.statusCode.value()).isEqualTo(404)
        assertThat(response.body)
            .contains("""window.__VIEW__ = "NotFound"""")
            .contains("""<script src="/springreact/springreact.js?""")
    }
}
