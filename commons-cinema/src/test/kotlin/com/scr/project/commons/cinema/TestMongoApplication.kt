package com.scr.project.commons.cinema

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer

@SpringBootApplication(scanBasePackages = ["com.scr.project"], proxyBeanMethods = false)
open class TestMongoApplication {

    companion object {

        val mongoDBContainer = MongoDBContainer("mongo:6.0").apply {
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }
    }
}