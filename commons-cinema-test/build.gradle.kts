plugins {
    kotlin("jvm") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.scr.project.commons.cinema.test"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

private val kMongoVersion = "4.10.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.litote.kmongo:kmongo:$kMongoVersion")
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