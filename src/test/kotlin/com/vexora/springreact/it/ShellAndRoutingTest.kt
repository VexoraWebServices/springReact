package com.vexora.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

/**
 * Verifies the zero-frontend promise (the shell loads the runtime bundled in the jar) and
 * convention-based routing (`@Route` screens are served, with the client route table).
 */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShellAndRoutingTest {

    @Autowired
    lateinit var rest: TestRestTemplate

    @Test
    fun shellLoadsBundledRuntime() {
        val html = rest.getForObject("/", String::class.java)
        assertThat(html)
            .contains("""<div id="root"></div>""")
            .contains("""window.__VIEW__ = "Home"""")
            .contains("""<script src="/springreact/springreact.js">""")
    }

    @Test
    fun bundledRuntimeIsServedFromTheJar() {
        val js = rest.getForEntity("/springreact/springreact.js", String::class.java)
        assertThat(js.statusCode.value()).isEqualTo(200)
        assertThat(js.body).isNotBlank()
    }

    @Test
    fun annotationRoutesServeShellsWithRouteTable() {
        val home = rest.getForObject("/", String::class.java)
        assertThat(home)
            .contains("""window.__VIEW__ = "Home"""")
            .contains(""""/users"""")
            .contains(""""view":"Users"""")
            .contains(""""layout":"Main"""")

        val users = rest.getForObject("/users", String::class.java)
        assertThat(users).contains("""window.__VIEW__ = "Users"""")
    }
}
