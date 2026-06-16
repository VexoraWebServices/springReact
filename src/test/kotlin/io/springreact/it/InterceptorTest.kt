package io.springreact.it

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Interceptors run before every action and can record/block them. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InterceptorTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var recorder: RecordingInterceptor

    private val inbox = LinkedBlockingQueue<String>()

    @Test
    fun interceptorRecordsAndBlocksActions() {
        val session = openLiveSocket()
        session.sendMessage(TextMessage("""{"t":"mount","id":"g","c":"Guarded2"}"""))
        next() // tree

        session.sendMessage(TextMessage("""{"t":"call","id":"g","action":"ok","args":[]}"""))
        assertThat(next()).contains("Value: 1")

        session.sendMessage(TextMessage("""{"t":"call","id":"g","action":"forbidden","args":[]}"""))
        assertThat(next()).contains(""""t":"error"""").contains("was blocked")

        // Interceptor saw both calls with the component name.
        assertThat(recorder.seen).contains("Guarded2#ok", "Guarded2#forbidden")

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
