package com.vexora.springreact.gradle

import org.gradle.api.provider.Property

/**
 * Configuration for the SpringReact Gradle plugin:
 *
 * ```
 * springReact {
 *     version.set("0.1.0")   // SpringReact framework version
 *     javaVersion.set(21)    // JDK toolchain
 * }
 * ```
 */
interface SpringReactExtension {
    /** The SpringReact framework version to depend on. */
    val version: Property<String>

    /** The Java toolchain version. */
    val javaVersion: Property<Int>
}
