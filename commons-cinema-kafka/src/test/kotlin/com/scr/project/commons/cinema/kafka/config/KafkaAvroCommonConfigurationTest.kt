package com.scr.project.commons.cinema.kafka.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KafkaAvroCommonConfigurationTest {

    @Test
    fun `kafkaAvroCommonProperties should succeed with mandatory properties`() {
        val props = KafkaAvroCommonConfiguration.kafkaAvroCommonProperties(
            "localhost:9092",
            "http://schema:8081",
            "SASL_PLAINTEXT",
            "PLAIN",
            null,
            null
        )
        assertThat(props["bootstrap.servers"]).isEqualTo("localhost:9092")
        assertThat(props["schema.registry.url"]).isEqualTo("http://schema:8081")
        assertThat(props["security.protocol"]).isEqualTo("SASL_PLAINTEXT")
        assertThat(props["sasl.mechanism"]).isEqualTo("PLAIN")
        assertThat(props).doesNotContainKey("sasl.jaas.config")
    }

    @Test
    fun `kafkaAvroCommonProperties should succeed with optional properties`() {
        val props = KafkaAvroCommonConfiguration.kafkaAvroCommonProperties(
            "localhost:9092",
            "http://schema:8081",
            "SASL_PLAINTEXT",
            "PLAIN",
            "user",
            "pass"
        )
        assertThat(props["sasl.jaas.config"]).isEqualTo(
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"pass\";"
        )
    }
}