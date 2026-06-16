plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "com.vexora.springreact"
version = "0.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // The plugins our plugin applies on behalf of the user must be on its classpath.
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.4.1")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.1.0")
}

gradlePlugin {
    plugins {
        create("springreact") {
            id = "com.vexora.springreact"
            implementationClass = "com.vexora.springreact.gradle.SpringReactPlugin"
            displayName = "SpringReact"
            description = "Configures a Kotlin + Spring Boot app to use the SpringReact framework"
        }
    }
}
