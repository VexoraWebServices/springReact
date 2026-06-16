package io.springreact.it;

import org.springframework.stereotype.Service;

/** An ordinary Spring bean, injected into a Java Server Component to prove DI works. */
@Service
public class GreetingService {

    public String hello(String who) {
        return "Hello, " + who + "!";
    }
}
