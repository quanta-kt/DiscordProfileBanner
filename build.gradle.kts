import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    application
}

group = "me.abhijeet"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven("https://jitpack.io/")
}

dependencies {
    implementation("io.ktor:ktor-server-core:1.6.3")
    implementation("io.ktor:ktor-server-netty:1.6.3")
    implementation("ch.qos.logback:logback-classic:1.2.5")

    implementation("com.discord4j:discord4j-core:3.1.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.1")

    implementation("org.imgscalr:imgscalr-lib:4.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("ApplicationKt")
}

tasks.create("stage") {
    dependsOn("installDist")
}
