pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val kotlinJVMVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinJVMVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}
rootProject.name = "commons-cinema-library"
include("commons-cinema")
include("commons-cinema-test")
include("commons-cinema-kafka")
include("commons-cinema-outbox")