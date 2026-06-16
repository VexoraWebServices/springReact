package com.vexora.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Dynamic routes: the URL pattern `/users/{id}` is served, advertised in the route table
 * (with its title), and the `{id}` value is bound to a `@LiveParam` field on mount.
 */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DynamicRouteTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var rest: TestRestTemplate

    private val inbox = LinkedBlockingQueue<String>()

    @Test
    fun dynamicRouteIsAdvertisedWithTitle() {
        val home = rest.getForObject("/", String::class.java)
        assertThat(home)
            .contains(""""/users/{id}"""")
            .contains(""""view":"User"""")
            .contains(""""title":"User Detail"""")
    }

    @Test
    fun routeParamIsBoundToTheComponent() {
        val session = openLiveSocket()
        // The client sends the matched params with the mount.
        session.sendMessage(TextMessage("""{"t":"mount","id":"u","c":"User","params":{"id":"42"}}"""))
        assertThat(next()).contains(""""t":"tree"""").contains("User #42")
        session.close()
    }

    private fun openLiveSocket(): WebSocketSession =
        StandardWebSocketClient().execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    inbox.add(message.payload)
                }
            },
            "ws://localhost:$port/live",
        ).get(5, TimeUnit.SECONDS)

    private fun next(): String = inbox.poll(5, TimeUnit.SECONDS) ?: error("expected a live message")
}
