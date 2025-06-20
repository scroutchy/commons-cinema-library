package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM

class KafkaAvroCommonConfiguration {

    companion object {

        fun kafkaAvroCommonProperties(
            bootstrapServers: String,
            schemaRegistryUrl: String,
            securityProtocol: String,
            saslMechanism: String,
            saslUsername: String? = null,
            saslPassword: String? = null,
        ): Map<String, Any> {
            val commonProperties = mapOf(
                BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
                SECURITY_PROTOCOL_CONFIG to securityProtocol,
                SASL_MECHANISM to saslMechanism,
            )

            return if (saslUsername != null && saslPassword != null) {
                commonProperties + mapOf(SASL_JAAS_CONFIG to "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$saslUsername\" password=\"$saslPassword\";")
            } else {
                commonProperties
            }
        }
    }
}