package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [KafkaAvroCommonConfiguration::class])
@ActiveProfiles("test")
class KafkaAvroCommonConfigurationTest(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
    @Value("\${spring.kafka.security-protocol}") private val securityProtocol: String,
    @Value("\${spring.kafka.sasl.mechanism}") private val saslMechanism: String,
    @Value("\${spring.kafka.sasl.username}") private val saslUsername: String?,
    @Value("\${spring.kafka.sasl.password}") private val saslPassword: String?,
) {

    private val configuration =
        KafkaAvroCommonConfiguration(bootstrapServers, schemaRegistryUrl, securityProtocol, saslMechanism, saslUsername, saslPassword)

    @Test
    fun `kafkaAvroCommonProperties contains mandatory properties`() {
        val properties = configuration.kafkaAvroCommonProperties()

        assertThat(properties[BOOTSTRAP_SERVERS_CONFIG]).isEqualTo("kafka:9092")
        assertThat(properties[SCHEMA_REGISTRY_URL_CONFIG]).isEqualTo("http://schema-registry:8081")
        assertThat(properties[SECURITY_PROTOCOL_CONFIG]).isEqualTo("SASL_PLAINTEXT")
        assertThat(properties[SASL_MECHANISM]).isEqualTo("PLAIN")
    }

    @Test
    fun `kafkaAvroCommonProperties contains optional properties`() {
        val properties = configuration.kafkaAvroCommonProperties()

        assertThat(properties["bootstrap.servers"]).isEqualTo("kafka:9092")
        assertThat(properties["schema.registry.url"]).isEqualTo("http://schema-registry:8081")
        assertThat(properties["security.protocol"]).isEqualTo("SASL_PLAINTEXT")
        assertThat(properties["sasl.mechanism"]).isEqualTo("PLAIN")
        assertThat(properties["sasl.jaas.config"]).isEqualTo("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"username\" password=\"password\";")
    }

    @Test
    fun `kafkaAvroCommonProperties does not contain optional properties if not set`() {
        val noCredentialConfiguration =
            KafkaAvroCommonConfiguration(bootstrapServers, schemaRegistryUrl, securityProtocol, saslMechanism, null, null)
        val properties = noCredentialConfiguration.kafkaAvroCommonProperties()

        assertThat(properties["bootstrap.servers"]).isEqualTo("kafka:9092")
        assertThat(properties["schema.registry.url"]).isEqualTo("http://schema-registry:8081")
        assertThat(properties["security.protocol"]).isEqualTo("SASL_PLAINTEXT")
        assertThat(properties["sasl.mechanism"]).isEqualTo("PLAIN")
        assertThat(properties["sasl.jaas.config"]).isNull()
    }
}