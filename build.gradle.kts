plugins {
    kotlin("jvm") version "2.1.20"
    id("org.springframework.boot") version "3.4.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
    id("org.sonarqube") version "6.0.1.5171"
}

group = "org.scr.project"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

tasks.bootJar {
    enabled = false
}

sonar {
    properties {
        property("sonar.projectKey", "cinema7590904_commons-cinema-library")
        property("sonar.organization", "cinema7590904")
    }
}