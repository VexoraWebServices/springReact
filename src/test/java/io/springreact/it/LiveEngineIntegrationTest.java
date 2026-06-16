package io.springreact.it;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives Java Server Components over the real {@code /live} WebSocket and asserts the
 * server streams a full React tree on mount and minimal patches on updates. Covers DI, the
 * custom-widget escape hatch, server-side diffing, and Kotlin-authored components.
 */
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LiveEngineIntegrationTest {

    @LocalServerPort
    int port;

    private final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();

    @Test
    void javaScreenRendersTreeThenPatches() throws Exception {
        WebSocketSession session = openLiveSocket();

        // Mount → full tree. DI (GreetingService) + custom widget present.
        session.sendMessage(new TextMessage("{\"t\":\"mount\",\"id\":\"jc\",\"c\":\"JavaCounter\"}"));
        String mounted = next();
        assertThat(mounted)
                .contains("\"t\":\"tree\"")
                .contains("Hello, Java!")            // injected service
                .contains("Count: 0")
                .contains("\"$widget\"")             // custom widget node
                .contains("\"name\":\"StarRating\"")
                .contains("\"value\":0");

        // Action → a minimal PATCH (not a full tree), with just the changed text.
        session.sendMessage(new TextMessage("{\"t\":\"call\",\"id\":\"jc\",\"action\":\"increment\",\"args\":[]}"));
        String afterInc = next();
        assertThat(afterInc).contains("\"t\":\"patch\"").contains("Count: 1");
        assertThat(afterInc).doesNotContain("Hello, Java!"); // unchanged subtree not resent

        // Widget-driven action updates only the widget's prop.
        session.sendMessage(new TextMessage("{\"t\":\"call\",\"id\":\"jc\",\"action\":\"rate\",\"args\":[5]}"));
        String afterRate = next();
        assertThat(afterRate).contains("\"t\":\"patch\"").contains("\"value\":5");

        session.close();
    }

    @Test
    void kotlinScreenWorksToo() throws Exception {
        WebSocketSession session = openLiveSocket();

        session.sendMessage(new TextMessage("{\"t\":\"mount\",\"id\":\"kg\",\"c\":\"KotlinGreet\"}"));
        assertThat(next()).contains("\"t\":\"tree\"").contains("Hi, world");

        session.sendMessage(new TextMessage("{\"t\":\"call\",\"id\":\"kg\",\"action\":\"setName\",\"args\":[\"Spring\"]}"));
        assertThat(next()).contains("\"t\":\"patch\"").contains("Hi, Spring");

        session.close();
    }

    private WebSocketSession openLiveSocket() throws Exception {
        return new StandardWebSocketClient()
                .execute(new TextWebSocketHandler() {
                    @Override
                    protected void handleTextMessage(WebSocketSession s, TextMessage m) {
                        inbox.add(m.getPayload());
                    }
                }, "ws://localhost:" + port + "/live")
                .get(5, TimeUnit.SECONDS);
    }

    private String next() throws InterruptedException {
        String msg = inbox.poll(5, TimeUnit.SECONDS);
        assertThat(msg).as("expected a message from the live socket").isNotNull();
        return msg;
    }
}
