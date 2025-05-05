package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaAvroCommonConfiguration(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
    @Value("\${spring.kafka.security-protocol}") private val securityProtocol: String,
    @Value("\${spring.kafka.sasl.mechanism}") private val saslMechanism: String,
    @Value("\${spring.kafka.sasl.username}") private val saslUsername: String?,
    @Value("\${spring.kafka.sasl.password}") private val saslPassword: String?,
) {

    @Bean
    fun kafkaAvroCommonProperties(): Map<String, Any> {
        val commonProperties = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            SECURITY_PROTOCOL_CONFIG to securityProtocol,
            SASL_MECHANISM to saslMechanism,
        )

        if (saslUsername != null && saslPassword != null) {
            commonProperties + mapOf(SASL_JAAS_CONFIG to "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$saslUsername\" password=\"$saslPassword\";")
        }

        return commonProperties
    }
}