package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [KafkaAvroConsumerConfiguration::class, KafkaAvroCommonConfiguration::class])
@ActiveProfiles("test")
class KafkaAvroConsumerConfigurationTest(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
    @Value("\${spring.kafka.security-protocol}") private val securityProtocol: String,
    @Value("\${spring.kafka.sasl.mechanism}") private val saslMechanism: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
    ) {

    private val commonProperties = KafkaAvroCommonConfiguration(
        bootstrapServers, schemaRegistryUrl, securityProtocol, saslMechanism, "username", "password"
    ).kafkaAvroCommonProperties()
    private val configuration = KafkaAvroConsumerConfiguration(commonProperties, groupId)

    @Test
    fun `kafkaAvroConsumerProperties contains mandatory properties`() {
        val properties = configuration.kafkaAvroConsumerProperties()

        assertThat(properties[BOOTSTRAP_SERVERS_CONFIG]).isEqualTo(bootstrapServers)
        assertThat(properties[SCHEMA_REGISTRY_URL_CONFIG]).isEqualTo(schemaRegistryUrl)
        assertThat(properties[SECURITY_PROTOCOL_CONFIG]).isEqualTo(securityProtocol)
        assertThat(properties[SASL_MECHANISM]).isEqualTo(saslMechanism)
        assertThat(properties[GROUP_ID_CONFIG]).isEqualTo(groupId)
        assertThat(properties[KEY_DESERIALIZER_CLASS_CONFIG]).isEqualTo(StringDeserializer::class.java)
        assertThat(properties[VALUE_DESERIALIZER_CLASS_CONFIG]).isEqualTo(KafkaAvroDeserializer::class.java)
        assertThat(properties[SPECIFIC_AVRO_READER_CONFIG]).isEqualTo(true)
    }

    @Test
    fun `kafkaAvroConsumerProperties overloads groupId in case it was already in common properties`() {
        val commonWithGroup = commonProperties + (GROUP_ID_CONFIG to "other-group")
        val config = KafkaAvroConsumerConfiguration(commonWithGroup, groupId)
        val properties = config.kafkaAvroConsumerProperties()

        assertThat(properties[GROUP_ID_CONFIG]).isEqualTo(groupId)
    }
}