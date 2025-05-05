package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [KafkaAvroProducerConfiguration::class, KafkaAvroCommonConfiguration::class])
@ActiveProfiles("test")
class KafkaAvroProducerConfigurationTest(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
    @Value("\${spring.kafka.security-protocol}") private val securityProtocol: String,
    @Value("\${spring.kafka.sasl.mechanism}") private val saslMechanism: String,
) {

    private val commonProperties = KafkaAvroCommonConfiguration(
        bootstrapServers, schemaRegistryUrl, securityProtocol, saslMechanism, "username", "password"
    ).kafkaAvroCommonProperties()
    private val configuration = KafkaAvroProducerConfiguration(commonProperties)

    @Test
    fun `kafkaAvroProducerProperties contains mandatory properties`() {
        val properties = configuration.kafkaAvroProducerProperties()

        assertThat(properties[BOOTSTRAP_SERVERS_CONFIG]).isEqualTo(bootstrapServers)
        assertThat(properties[SCHEMA_REGISTRY_URL_CONFIG]).isEqualTo(schemaRegistryUrl)
        assertThat(properties[SECURITY_PROTOCOL_CONFIG]).isEqualTo(securityProtocol)
        assertThat(properties[SASL_MECHANISM]).isEqualTo(saslMechanism)
        assertThat(properties[KEY_SERIALIZER_CLASS_CONFIG]).isEqualTo(StringSerializer::class.java)
        assertThat(properties[VALUE_SERIALIZER_CLASS_CONFIG]).isEqualTo(KafkaAvroSerializer::class.java)
    }
}