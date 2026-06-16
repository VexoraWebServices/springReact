// The whole setup: one plugin. It applies Kotlin, Kotlin-Spring, Spring Boot, dependency
// management, and the SpringReact framework dependency.
plugins {
    id("com.vexora.springreact") version "0.1.0"
}

group = "com.example"
version = "0.0.1"

springReact {
    version.set("0.1.0")
    javaVersion.set(21)
}
