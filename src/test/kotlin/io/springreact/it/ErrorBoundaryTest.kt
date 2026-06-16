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

/** A render() failure shows a friendly error UI instead of crashing the connection. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorBoundaryTest {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun renderFailureShowsTheDefaultErrorUi() {
        val inbox = LinkedBlockingQueue<String>()
        val session = open(inbox)
        session.sendMessage(TextMessage("""{"t":"mount","id":"e","c":"Throwing"}"""))
        val msg = inbox.poll(5, TimeUnit.SECONDS) ?: error("no message")
        assertThat(msg).contains(""""t":"tree"""").contains("Something went wrong").contains("boom")
        session.close()
    }

    private fun open(inbox: LinkedBlockingQueue<String>): WebSocketSession =
        StandardWebSocketClient().execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    inbox.add(message.payload)
                }
            },
            "ws://localhost:$port/live",
        ).get(5, TimeUnit.SECONDS)
}

/** With spring.react.error-view set, render failures use that custom component. */
@SpringBootTest(
    classes = [TestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.react.error-view=ErrorView"],
)
class CustomErrorViewTest {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun renderFailureUsesTheConfiguredErrorComponent() {
        val inbox = LinkedBlockingQueue<String>()
        val session = StandardWebSocketClient().execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    inbox.add(message.payload)
                }
            },
            "ws://localhost:$port/live",
        ).get(5, TimeUnit.SECONDS)

        session.sendMessage(TextMessage("""{"t":"mount","id":"e","c":"Throwing"}"""))
        val msg = inbox.poll(5, TimeUnit.SECONDS) ?: error("no message")
        assertThat(msg).contains(""""t":"tree"""").contains("Oops").contains("boom")
        session.close()
    }
}
