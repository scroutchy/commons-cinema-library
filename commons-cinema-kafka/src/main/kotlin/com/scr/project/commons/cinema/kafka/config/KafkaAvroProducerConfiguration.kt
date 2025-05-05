package com.scr.project.commons.cinema.kafka.config

import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaAvroProducerConfiguration(private val kafkaAvroCommonsProperties: Map<String, Any>) {

    @Bean
    fun kafkaAvroProducerProperties(): Map<String, Any> {
        return kafkaAvroCommonsProperties + mapOf(
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
        )
    }
}