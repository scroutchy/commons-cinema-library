package com.scr.project.commons.cinema.kafka.config

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ContextConfiguration(classes = [KafkaAvroConsumerConfiguration::class, KafkaAvroCommonConfiguration::class])
@TestPropertySource(
    properties = [
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.schema.registry.url=http://localhost:8081",
        "spring.kafka.security-protocol=PLAINTEXT",
        "spring.kafka.sasl.mechanism=PLAIN",
        "spring.kafka.sasl.username=username",
        "spring.kafka.sasl.password=password"
    ]
)
class KafkaAvroConsumerConfigurationMissingGroupIdTest(@Autowired private val context: ApplicationContext) {

    @Test
    fun `configuration is not loaded if group-id is not in properties`() {
        assertThatThrownBy { context.getBean(KafkaAvroConsumerConfiguration::class.java) }.isInstanceOf(NoSuchBeanDefinitionException::class.java)
    }
}