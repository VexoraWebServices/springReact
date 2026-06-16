package io.springreact.it;

import io.springreact.jsc.ServerComponent;
import io.springreact.jsc.UiNode;
import io.springreact.live.LiveAction;
import io.springreact.live.LiveComponent;
import io.springreact.live.LiveState;

import static io.springreact.jsc.Html.*;

/** A Java-authored screen exercising DI, the Html DSL, and a custom widget. */
@LiveComponent("JavaCounter")
public class JavaCounterComponent implements ServerComponent {

    private final GreetingService greetings;

    public JavaCounterComponent(GreetingService greetings) {
        this.greetings = greetings;
    }

    @LiveState
    int count = 0;

    @LiveState
    int rating = 0;

    @LiveAction
    void increment() {
        count++;
    }

    @LiveAction
    void rate(int stars) {
        rating = stars;
    }

    @Override
    public UiNode render() {
        return div(cls("counter"),
                h1(greetings.hello("Java")),
                span(cls("count"), "Count: " + count),
                button(onClick("increment"), "+"),
                widget("StarRating", attr("value", rating), attr("action", "rate")));
    }
}
