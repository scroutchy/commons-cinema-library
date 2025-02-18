plugins {
    kotlin("jvm") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
}

group = "org.scr.project"
version = "1.0-SNAPSHOT"


private val kMongoVersion = "4.10.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

tasks.bootJar {
    enabled = false
}
