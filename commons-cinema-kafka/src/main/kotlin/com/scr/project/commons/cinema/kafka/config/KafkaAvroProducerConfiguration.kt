package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaAvroProducerConfiguration(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
    @Value("\${spring.kafka.security-protocol}") private val securityProtocol: String,
    @Value("\${spring.kafka.sasl.mechanism}") private val saslMechanism: String,
    @Value("\${spring.kafka.sasl.username}") private val saslUsername: String?,
    @Value("\${spring.kafka.sasl.password}") private val saslPassword: String?,
) {

    @Bean
    open fun kafkaAvroProducerProperties(): Map<String, Any> {
        return KafkaAvroCommonConfiguration.kafkaAvroCommonProperties(
            bootstrapServers,
            schemaRegistryUrl,
            securityProtocol,
            saslMechanism,
            saslUsername,
            saslPassword
        ) + mapOf(
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
        )
    }
}