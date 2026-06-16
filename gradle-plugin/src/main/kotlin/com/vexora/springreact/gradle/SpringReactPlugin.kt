package com.vexora.springreact.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Apply with `plugins { id("com.vexora.springreact") version "0.1.0" }` and your build is ready to
 * write SpringReact server components — it applies Kotlin, the Kotlin-Spring compiler
 * plugin, Spring Boot, dependency management, and the SpringReact dependency itself.
 *
 * Everything is configurable via the `springReact { }` extension.
 */
class SpringReactPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("springReact", SpringReactExtension::class.java)
        ext.version.convention(DEFAULT_VERSION)
        ext.javaVersion.convention(21)

        with(project.pluginManager) {
            apply("org.jetbrains.kotlin.jvm")
            apply("org.jetbrains.kotlin.plugin.spring")
            apply("org.springframework.boot")
            apply("io.spring.dependency-management")
        }

        project.repositories.mavenLocal()
        project.repositories.mavenCentral()

        // Configure the toolchain + dependency once the project is evaluated, so the
        // extension values the user set in build.gradle.kts are applied.
        project.afterEvaluate {
            project.extensions.findByType(JavaPluginExtension::class.java)?.let { java ->
                java.toolchain.languageVersion.set(JavaLanguageVersion.of(ext.javaVersion.get()))
            }
            project.dependencies.add("implementation", "com.vexora.springreact:SpringReact:${ext.version.get()}")
        }
    }

    companion object {
        const val DEFAULT_VERSION = "0.1.0"
    }
}
