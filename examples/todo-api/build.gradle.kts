plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
kotlin { jvmToolchain(21) }

repositories {
    mavenLocal() // resolves com.vexora.springreact published via publishToMavenLocal
    mavenCentral()
}

dependencies {
    // SpringReact provides the UI layer (and pulls in spring-web, so RestClient is available).
    implementation("com.vexora.springreact:SpringReact:0.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
