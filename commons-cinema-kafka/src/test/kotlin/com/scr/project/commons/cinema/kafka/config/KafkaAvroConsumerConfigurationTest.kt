package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
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
    @Value("\${spring.kafka.schema.registry.url}") private val schemaRegistryUrl: String,
    @Value("\${spring.kafka.security-protocol}") private val securityProtocol: String,
    @Value("\${spring.kafka.sasl.mechanism}") private val saslMechanism: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
    @Value("\${spring.kafka.sasl.username}") private val username: String,
    @Value("\${spring.kafka.sasl.password}") private val password: String
) {

    private val configuration = KafkaAvroConsumerConfiguration(
        bootstrapServers,
        schemaRegistryUrl,
        securityProtocol,
        saslMechanism,
        groupId,
        username,
        password,
    )

    @Test
    fun `kafkaAvroConsumerProperties contains properties`() {
        val properties = configuration.kafkaAvroConsumerProperties()

        assertThat(properties[BOOTSTRAP_SERVERS_CONFIG]).isEqualTo(bootstrapServers)
        assertThat(properties["schema.registry.url"]).isEqualTo(schemaRegistryUrl)
        assertThat(properties[SECURITY_PROTOCOL_CONFIG]).isEqualTo(securityProtocol)
        assertThat(properties[SASL_MECHANISM]).isEqualTo(saslMechanism)
        assertThat(properties[GROUP_ID_CONFIG]).isEqualTo(groupId)
        assertThat(properties[KEY_DESERIALIZER_CLASS_CONFIG]).isEqualTo(StringDeserializer::class.java)
        assertThat(properties[VALUE_DESERIALIZER_CLASS_CONFIG]).isEqualTo(KafkaAvroDeserializer::class.java)
        assertThat(properties[SPECIFIC_AVRO_READER_CONFIG]).isEqualTo(true)
        assertThat(properties[SASL_JAAS_CONFIG]).isEqualTo("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"username\" password=\"password\";")
    }
}