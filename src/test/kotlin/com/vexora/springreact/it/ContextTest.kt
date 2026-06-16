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

/** Server-initiated redirect + async self-update via LiveContext. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContextTest {

    @LocalServerPort
    var port: Int = 0

    private val inbox = LinkedBlockingQueue<String>()

    @Test
    fun actionCanRedirectTheClient() {
        val session = openLiveSocket()
        session.sendMessage(TextMessage("""{"t":"mount","id":"r","c":"Redirector"}"""))
        next() // initial tree
        session.sendMessage(TextMessage("""{"t":"call","id":"r","action":"go","args":[]}"""))
        assertThat(next()).contains(""""t":"navigate"""").contains(""""path":"/users"""")
        session.close()
    }

    @Test
    fun asyncWorkPushesAnUpdateLater() {
        val session = openLiveSocket()
        session.sendMessage(TextMessage("""{"t":"mount","id":"a","c":"Async"}"""))
        assertThat(next()).contains("Status: idle")

        session.sendMessage(TextMessage("""{"t":"call","id":"a","action":"load","args":[]}"""))
        // First the synchronous result of the action…
        assertThat(next()).contains("Status: loading")
        // …then a pushed update from the background thread.
        assertThat(next()).contains("Status: loaded")

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
