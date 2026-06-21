plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "org.ron.webrtccall"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("org.ron.webrtccall.server.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.json)
    implementation(libs.logback.classic)
}

repositories {
    mavenCentral()
}
