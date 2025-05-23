plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java-library")
    id("maven-publish")
}

group = "com.scr.project.commons.cinema.test"
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

repositories {
    mavenCentral()
}
private val kMongoVersion: String by project
private val netMinidevVersion: String by project
private val nettyHandlerVersion: String by project
private val commonsCompressVersion: String by project

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.litote.kmongo:kmongo:$kMongoVersion")
    implementation("net.minidev:json-smart:$netMinidevVersion")
    implementation("io.netty:netty-handler:$nettyHandlerVersion")
    implementation("io.netty:netty-common:$nettyHandlerVersion")
    implementation("org.apache.commons:commons-compress:$commonsCompressVersion")
    implementation("org.awaitility:awaitility")
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
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

publishing {

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "commons-cinema-test"
            version = version
        }
    }

    repositories {
        maven {
            url = uri("${System.getenv("CI_API_V4_URL")}/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
            credentials(HttpHeaderCredentials::class.java) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication { create("header", HttpHeaderAuthentication::class.java) }
        }
    }
}
