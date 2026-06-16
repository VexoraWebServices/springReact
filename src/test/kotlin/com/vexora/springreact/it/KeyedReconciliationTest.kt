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
 * Removing a list item produces a minimal keyed patch — the surviving items are referenced
 * by key (no node payload), not re-sent as text. This is keyed reconciliation, not index
 * churn.
 */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeyedReconciliationTest {

    @LocalServerPort
    var port: Int = 0

    private val inbox = LinkedBlockingQueue<String>()

    @Test
    fun removingAnItemEmitsAMinimalKeyedPatch() {
        val session = openLiveSocket()

        session.sendMessage(TextMessage("""{"t":"mount","id":"k","c":"Keyed"}"""))
        assertThat(next()).contains(""""t":"tree"""").contains("Item 1").contains("Item 3")

        session.sendMessage(TextMessage("""{"t":"call","id":"k","action":"removeFirst","args":[]}"""))
        val patch = next()
        assertThat(patch).contains(""""t":"patch"""").contains(""""op":"keyed"""")
        // surviving items referenced by key only — their text is NOT re-sent
        assertThat(patch).contains(""""key":2""").contains(""""key":3""")
        assertThat(patch).doesNotContain("Item ")

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
