plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1" // Correct syntax for Kotlin DSL
}

group = "org.example"
version = "0.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("net.portswigger.burp.extensions:montoya-api:2025.2")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.test {
    useJUnitPlatform()
}

// Correct Kotlin syntax for shadowJar task
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}

// Ensure build depends on shadowJar
tasks.build {
    dependsOn(tasks.named("shadowJar"))
}
