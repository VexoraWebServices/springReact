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
 * A submitted form binds to a typed DTO and is validated with Bean Validation. Invalid
 * input renders a field error (server state); valid input is accepted.
 */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FormValidationTest {

    @LocalServerPort
    var port: Int = 0

    private val inbox = LinkedBlockingQueue<String>()

    @Test
    fun invalidFormShowsErrorAndValidFormIsAccepted() {
        val session = openLiveSocket()

        session.sendMessage(TextMessage("""{"t":"mount","id":"f","c":"Form"}"""))
        assertThat(next()).contains(""""t":"tree"""")

        // Empty title violates @NotBlank → field error rendered.
        session.sendMessage(TextMessage("""{"t":"call","id":"f","action":"save","args":[{"title":""}]}"""))
        assertThat(next()).contains(""""t":"patch"""").contains("title is required")

        // Valid title → item added.
        session.sendMessage(TextMessage("""{"t":"call","id":"f","action":"save","args":[{"title":"Buy milk"}]}"""))
        assertThat(next()).contains(""""t":"patch"""").contains("Buy milk")

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
