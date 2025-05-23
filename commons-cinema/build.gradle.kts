plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java-library")
    id("maven-publish")
    id("jacoco")
}

group = "com.scr.project.commons.cinema"

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
private val netMinidevVersion: String by project
private val nettyHandlerVersion: String by project
private val commonsCompressVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("net.minidev:json-smart:$netMinidevVersion")
    implementation("io.netty:netty-handler:$nettyHandlerVersion")
    implementation("io.netty:netty-common:$nettyHandlerVersion")
    implementation("org.apache.commons:commons-compress:$commonsCompressVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    testImplementation("io.projectreactor:reactor-test")
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

tasks.register("printCoverage") {
    group = "verification"
    description = "Prints the code coverage of the project"
    dependsOn(tasks.jacocoTestReport)
    doLast {
        val reportFile = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile
        if (reportFile.exists()) {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(reportFile)
            val counters = document.getElementsByTagName("counter")
            var covered = 0
            var missed = 0
            for (i in 0 until counters.length) {
                val counter = counters.item(i) as org.w3c.dom.Element
                covered += counter.getAttribute("covered").toInt()
                missed += counter.getAttribute("missed").toInt()
            }
            val totalCoverage = (covered * 100.0) / (covered + missed)
            println("Total Code Coverage: %.2f%%".format(totalCoverage))
        } else {
            println("JaCoCo report file not found!")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport", tasks.named("printCoverage"))
}

publishing {

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "commons-cinema"
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