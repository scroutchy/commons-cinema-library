package com.scr.project.commons.cinema.kafka.consumer

import com.scr.project.commons.cinema.kafka.processor.KafkaProcessor
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KafkaConsumerInitializer(private val kafkaProcessors: List<KafkaProcessor<*>>) {

    private val logger: Logger = LoggerFactory.getLogger(KafkaConsumerInitializer::class.java)

    @PostConstruct
    fun initialize() {
        kafkaProcessors.forEach {
            logger.info("Initializing ${it::class.simpleName} consumer")
            it.startConsuming()
                .doOnError { e -> logger.warn("Error while initializing ${it::class.simpleName} consumer: ${e.message}") }
                .doOnComplete { logger.info("${it::class.simpleName} consumer initialized") }
                .subscribe()
        }
    }
}