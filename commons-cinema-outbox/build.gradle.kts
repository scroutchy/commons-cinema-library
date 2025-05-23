plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java-library")
    id("maven-publish")
    id("jacoco")
}

group = "org.scr.project.commons.cinema"

fun getGitTag(): String {
    return try {
        val process = ProcessBuilder("git", "describe", "--tags").start()
        val result = process.inputStream.bufferedReader().readText().trim()
        process.waitFor() // Wait for the process to finish
        result
    } catch (e: Exception) {
        "0.0.1-SNAPSHOT" // Default version if no tag exists
    }
}
version = getGitTag()
private val reactorKafkaVersion: String by project
private val mockkVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("io.projectreactor.kafka:reactor-kafka:$reactorKafkaVersion")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

tasks.bootJar {
    enabled = false
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}