package com.vexora.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Drives Kotlin server components over the real `/live` WebSocket: full tree on mount,
 * minimal patches on updates. Covers DI, the custom-widget hatch, and diffing.
 */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LiveEngineIntegrationTest {

    @LocalServerPort
    var port: Int = 0

    private val inbox = LinkedBlockingQueue<String>()

    @Test
    fun homeScreenRendersTreeThenPatches() {
        val session = openLiveSocket()

        session.sendMessage(TextMessage("""{"t":"mount","id":"h","c":"Home"}"""))
        val mounted = next()
        assertThat(mounted)
            .contains(""""t":"tree"""")
            .contains("Hello, Home!")
            .contains("Count: 0")
            .contains(""""${'$'}widget"""")
            .contains(""""name":"StarRating"""")
            .contains(""""value":0""")

        session.sendMessage(TextMessage("""{"t":"call","id":"h","action":"increment","args":[]}"""))
        val afterInc = next()
        assertThat(afterInc).contains(""""t":"patch"""").contains("Count: 1")
        assertThat(afterInc).doesNotContain("Hello, Home!")

        session.sendMessage(TextMessage("""{"t":"call","id":"h","action":"rate","args":[5]}"""))
        assertThat(next()).contains(""""t":"patch"""").contains(""""value":5""")

        session.close()
    }

    @Test
    fun kotlinInputComponentUpdates() {
        val session = openLiveSocket()

        session.sendMessage(TextMessage("""{"t":"mount","id":"g","c":"KotlinGreet"}"""))
        assertThat(next()).contains(""""t":"tree"""").contains("Hi, world")

        session.sendMessage(TextMessage("""{"t":"call","id":"g","action":"setName","args":["Spring"]}"""))
        assertThat(next()).contains(""""t":"patch"""").contains("Hi, Spring")

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
