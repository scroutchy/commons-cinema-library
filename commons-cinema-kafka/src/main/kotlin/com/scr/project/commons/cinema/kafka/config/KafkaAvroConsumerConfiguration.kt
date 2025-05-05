package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaAvroConsumerConfiguration(
    private val kafkaAvroCommonsProperties: Map<String, Any>,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String
) {

    @Bean
    open fun kafkaAvroConsumerProperties(): Map<String, Any> {
        return kafkaAvroCommonsProperties + mapOf(
            GROUP_ID_CONFIG to groupId,
            KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
            SPECIFIC_AVRO_READER_CONFIG to true,
        )
    }
}