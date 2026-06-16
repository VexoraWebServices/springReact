package io.springreact.it

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Service

/** Minimal Spring Boot app hosting the framework's auto-configuration during tests. */
@SpringBootApplication
class TestApp

/** An ordinary Spring bean, injected into a server component to prove DI works. */
@Service
class GreetingService {
    fun hello(who: String) = "Hello, $who!"
}
