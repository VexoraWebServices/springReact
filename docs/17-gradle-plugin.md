# 17. The Gradle Plugin

The fastest way to start: apply the **`io.springreact` Gradle plugin**. It configures
everything — Kotlin, the Kotlin-Spring compiler plugin, Spring Boot, dependency management,
and the SpringReact dependency — so your build file is one line.

## Use it

`build.gradle.kts`:

```kotlin
plugins {
    id("io.springreact") version "0.1.0"
}
```

That's the entire setup. Now write `@LiveComponent` screens and run `./gradlew bootRun`.

Compare with doing it by hand:

```kotlin
// without the plugin you'd write all of this yourself:
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}
dependencies { implementation("io.springreact:SpringReact:0.1.0") }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
```

## Configure it

Optional `springReact { }` block:

```kotlin
springReact {
    version.set("0.1.0")   // SpringReact framework version (default: matches the plugin)
    javaVersion.set(21)    // JDK toolchain (default: 21)
}
```

## settings.gradle.kts

The plugin and framework resolve from Maven Central once published. For local development
against a locally-published build, point at Maven Local:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
```

## Try it

A complete minimal app using the plugin is in
[`examples/minimal`](../examples/minimal) — a single `build.gradle.kts` with the plugin and
one screen.

## TODO checklist

- [ ] Add `id("io.springreact") version "0.1.0"` to `plugins { }`
- [ ] Write a `@LiveComponent @Route("/")` screen
- [ ] `./gradlew bootRun`
