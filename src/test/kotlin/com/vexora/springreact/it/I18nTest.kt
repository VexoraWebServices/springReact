package com.vexora.springreact.it

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

/** The component renders messages in the client's Accept-Language locale. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class I18nTest {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun rendersSpanishForAcceptLanguageEs() {
        assertThat(mountWithLocale("es-ES,es;q=0.9")).contains("Hola")
    }

    @Test
    fun rendersEnglishByDefault() {
        assertThat(mountWithLocale("en-US,en;q=0.9")).contains("Hello")
    }

    private fun mountWithLocale(acceptLanguage: String): String {
        val inbox = LinkedBlockingQueue<String>()
        val headers = WebSocketHttpHeaders().apply { add("Accept-Language", acceptLanguage) }
        val session = StandardWebSocketClient().execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    inbox.add(message.payload)
                }
            },
            headers,
            URI("ws://localhost:$port/live"),
        ).get(5, TimeUnit.SECONDS)
        session.sendMessage(TextMessage("""{"t":"mount","id":"i","c":"I18n"}"""))
        val tree = inbox.poll(5, TimeUnit.SECONDS) ?: error("no message")
        session.close()
        return tree
    }
}
