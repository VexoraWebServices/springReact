package io.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

/** Routed pages render server-side SEO metadata (title + description) in the shell head. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetadataTest {

    @Autowired
    lateinit var rest: TestRestTemplate

    @Test
    fun routeTitleAndDescriptionAreRenderedServerSide() {
        val html = rest.getForObject("/users/5", String::class.java)
        assertThat(html)
            .contains("<title>User Detail</title>")
            .contains("""<meta name="description" content="A single user" />""")
    }

    @Test
    fun defaultTitleWhenRouteHasNone() {
        val html = rest.getForObject("/", String::class.java)
        assertThat(html).contains("<title>SpringReact</title>") // no title on the Home route
    }
}
