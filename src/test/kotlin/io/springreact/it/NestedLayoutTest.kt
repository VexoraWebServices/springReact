package io.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

/** The layout-nesting table (layout → parent) is advertised to the client. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NestedLayoutTest {

    @Autowired
    lateinit var rest: TestRestTemplate

    @Test
    fun nestedLayoutChainIsAdvertised() {
        val html = rest.getForObject("/dash", String::class.java)
        assertThat(html)
            .contains("""window.__VIEW__ = "Dash"""")
            .contains("""window.__LAYOUTS__""")
            .contains(""""Section":"Root"""") // Section is nested in Root
    }
}
