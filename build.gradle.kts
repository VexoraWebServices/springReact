import com.github.gradle.node.npm.task.NpmTask

// SpringReact — a Kotlin-first Spring Boot framework for "Server Components":
// author React screens in Kotlin (or Java), render them on the JVM, and stream the
// React element tree (plus incremental patches) to a runtime the framework BUNDLES
// inside its own jar and serves automatically.
//
// One command does everything: `./gradlew build` compiles Kotlin, bundles the React
// runtime into the jar, and runs the Spring + client test suites.
plugins {
    `java-library`
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.1.0"
    `maven-publish`
    signing
}

group = "io.springreact"
version = "0.1.0"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
    withSourcesJar()
    withJavadocJar() // Maven Central requires a javadoc jar
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1")
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-websocket")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// --- The bundled React runtime (built here, embedded in the jar) -------------------

node {
    download.set(false)
    nodeProjectDir.set(file("$projectDir/client"))
}

tasks.register<NpmTask>("clientBundle") {
    dependsOn("npmInstall")
    args.set(listOf("run", "build"))
    inputs.dir("$projectDir/client/src")
    inputs.files("$projectDir/client/package.json", "$projectDir/client/tsconfig.json")
    outputs.dir("$projectDir/client/dist")
}

tasks.register<NpmTask>("clientTest") {
    dependsOn("npmInstall")
    args.set(listOf("run", "test"))
    outputs.upToDateWhen { false }
}

tasks.register<NpmTask>("clientTypecheck") {
    dependsOn("npmInstall")
    args.set(listOf("run", "typecheck"))
    outputs.upToDateWhen { false }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("clientBundle")
    from("$projectDir/client/dist") {
        into("static/springreact")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy("clientTest", "clientTypecheck")
}

tasks.named("check") {
    dependsOn("clientTest", "clientTypecheck")
}

publishing {
    repositories {
        // Maven Central (Sonatype). Configured only when credentials are present, so a
        // normal build needs no secrets. Set OSSRH_USERNAME / OSSRH_PASSWORD to publish.
        val ossrhUser = providers.environmentVariable("OSSRH_USERNAME").orNull
        if (ossrhUser != null) {
            maven {
                name = "central"
                url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                credentials {
                    username = ossrhUser
                    password = providers.environmentVariable("OSSRH_PASSWORD").orNull
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("SpringReact")
                description.set("Kotlin-first Spring Boot framework for React server components over one WebSocket")
                url.set("https://github.com/VexoraWebServices/springReact")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("kumaraguru1735")
                        name.set("Kumaraguru")
                    }
                }
                scm {
                    url.set("https://github.com/VexoraWebServices/springReact")
                    connection.set("scm:git:https://github.com/VexoraWebServices/springReact.git")
                }
            }
        }
    }
}

// Sign the publication for Maven Central — only when a signing key is provided (so local
// builds don't need GPG). Provide SIGNING_KEY (ASCII-armored) and SIGNING_PASSWORD.
signing {
    val signingKey = providers.environmentVariable("SIGNING_KEY").orNull
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, providers.environmentVariable("SIGNING_PASSWORD").orNull)
        sign(publishing.publications["maven"])
    }
}
