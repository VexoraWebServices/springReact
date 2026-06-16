package io.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URI
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** An action can read handshake headers and cookies via LiveContext. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HeadersTest {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun actionReadsHeaderAndCookie() {
        val inbox = LinkedBlockingQueue<String>()
        val headers = WebSocketHttpHeaders().apply {
            add("X-Test", "hello")
            add("Cookie", "sid=abc123")
        }
        val session = StandardWebSocketClient().execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    inbox.add(message.payload)
                }
            },
            headers,
            URI("ws://localhost:$port/live"),
        ).get(5, TimeUnit.SECONDS)

        session.sendMessage(TextMessage("""{"t":"mount","id":"h","c":"Headers"}"""))
        inbox.poll(5, TimeUnit.SECONDS) // tree
        session.sendMessage(TextMessage("""{"t":"call","id":"h","action":"read","args":[]}"""))
        val patch = inbox.poll(5, TimeUnit.SECONDS) ?: error("no message")
        assertThat(patch).contains("H: hello").contains("C: abc123")

        session.close()
    }
}
