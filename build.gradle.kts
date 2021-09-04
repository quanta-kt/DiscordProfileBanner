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
    // Server
    implementation("io.ktor:ktor-server-core:1.6.3")
    implementation("io.ktor:ktor-server-netty:1.6.3")
    implementation("ch.qos.logback:logback-classic:1.2.5")

    // Discord API
    implementation("com.discord4j:discord4j-core:3.1.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.1")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.31.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.31.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.31.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.31.1")
    implementation("org.postgresql:postgresql:42.2.23")
    implementation("com.zaxxer:HikariCP:5.0.0")

    // DI
    implementation("io.insert-koin:koin-ktor:3.1.2")

    // Image processing
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
