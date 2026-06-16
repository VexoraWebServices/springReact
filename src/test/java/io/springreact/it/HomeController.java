package io.springreact.it;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Thymeleaf-style: return a view name; the framework renders the shell for it. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Home";
    }
}
