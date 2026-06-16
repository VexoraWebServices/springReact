package com.vexora.springreact.it

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
import java.util.function.IntSupplier

/** onMount / onUnmount fire on mount and on disconnect. */
@SpringBootTest(classes = [TestApp::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LifecycleTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var tracker: PresenceTracker

    @Test
    fun mountAndDisconnectFireLifecycleHooks() {
        val before = tracker.joined.get()
        val inbox = LinkedBlockingQueue<String>()
        val session = StandardWebSocketClient().execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    inbox.add(message.payload)
                }
            },
            "ws://localhost:$port/live",
        ).get(5, TimeUnit.SECONDS)

        session.sendMessage(TextMessage("""{"t":"mount","id":"p","c":"Presence"}"""))
        inbox.poll(5, TimeUnit.SECONDS) // tree
        await { tracker.joined.get() }.isAtLeast(before + 1)

        val leftBefore = tracker.left.get()
        session.close() // disconnect → onUnmount
        await { tracker.left.get() }.isAtLeast(leftBefore + 1)
    }

    private fun await(supplier: IntSupplier) = AwaitInt(supplier)

    class AwaitInt(private val supplier: IntSupplier) {
        fun isAtLeast(target: Int) {
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (System.nanoTime() < deadline) {
                if (supplier.asInt >= target) return
                Thread.sleep(20)
            }
            assertThat(supplier.asInt).isGreaterThanOrEqualTo(target)
        }
    }
}
