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
 * A `@LiveAuthorize` action runs only when the [io.springreact.live.LiveSecurity] bean
 * permits it. Here the test security allows EDITOR and denies ADMIN.
 */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorizationTest {

    @LocalServerPort
    var port: Int = 0

    private val inbox = LinkedBlockingQueue<String>()

    @Test
    fun allowedActionRunsAndDeniedActionIsRejected() {
        val session = openLiveSocket()

        session.sendMessage(TextMessage("""{"t":"mount","id":"s","c":"Secured"}"""))
        assertThat(next()).contains(""""t":"tree"""").contains("Value: 0")

        // EDITOR-guarded action is permitted → value 1.
        session.sendMessage(TextMessage("""{"t":"call","id":"s","action":"allowed","args":[]}"""))
        assertThat(next()).contains(""""t":"patch"""").contains("Value: 1")

        // ADMIN-guarded action is denied → error, state unchanged.
        session.sendMessage(TextMessage("""{"t":"call","id":"s","action":"denied","args":[]}"""))
        assertThat(next()).contains(""""t":"error"""").contains("Not authorized")

        // Prove the denied action did not mutate state: another allowed call yields 2, not 102.
        session.sendMessage(TextMessage("""{"t":"call","id":"s","action":"allowed","args":[]}"""))
        assertThat(next()).contains("Value: 2")

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
