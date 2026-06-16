package io.springreact.it

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
 * Two independent clients mount the same realtime screen. When one triggers a shared-state
 * change, the server broadcasts a re-render and BOTH clients receive the update.
 */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BroadcastTest {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun bumpFromOneClientUpdatesAllClients() {
        val a = Client()
        val b = Client()
        val sa = a.connect()
        val sb = b.connect()

        sa.sendMessage(TextMessage("""{"t":"mount","id":"d","c":"Dashboard"}"""))
        sb.sendMessage(TextMessage("""{"t":"mount","id":"d","c":"Dashboard"}"""))
        assertThat(a.next()).contains(""""t":"tree"""") // initial render for A
        assertThat(b.next()).contains(""""t":"tree"""") // initial render for B

        // A bumps the shared counter → server broadcasts to everyone.
        sa.sendMessage(TextMessage("""{"t":"call","id":"d","action":"bump","args":[]}"""))

        // B receives a pushed patch reflecting the new shared value, with no action of its own.
        assertThat(b.next()).contains(""""t":"patch"""").contains("Value: 1")

        sa.close()
        sb.close()
    }

    private inner class Client {
        val inbox = LinkedBlockingQueue<String>()
        fun connect(): WebSocketSession =
            StandardWebSocketClient().execute(
                object : TextWebSocketHandler() {
                    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                        inbox.add(message.payload)
                    }
                },
                "ws://localhost:$port/live",
            ).get(5, TimeUnit.SECONDS)

        fun next(): String = inbox.poll(5, TimeUnit.SECONDS) ?: error("expected a live message")
    }
}
