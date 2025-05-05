package com.scr.project.commons.cinema.kafka

import com.scr.project.commons.cinema.kafka.consumer.KafkaConsumerInitializer
import com.scr.project.commons.cinema.kafka.processor.KafkaProcessor
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

class KafkaConsumerInitializerTest {

    private val logger = mockk<Logger>(relaxed = true)
    private val kafkaProcessor1 = mockk<KafkaProcessor<*>>()
    private val kafkaProcessor2 = mockk<KafkaProcessor<*>>()
    private val kafkaConsumerInitializer = KafkaConsumerInitializer(listOf(kafkaProcessor1, kafkaProcessor2))

    @BeforeEach
    fun setUp() {
        clearMocks(kafkaProcessor1, kafkaProcessor2)
        mockkStatic(LoggerFactory::class)
        every { LoggerFactory.getLogger(any<Class<*>>()) } returns logger
    }

    @Test
    fun `initialize calls startConsuming and subscribes for each processor`() {
        every { kafkaProcessor1.startConsuming() } returns Flux.empty()
        every { kafkaProcessor2.startConsuming() } returns Flux.empty()

        kafkaConsumerInitializer.initialize()

        verify(exactly = 1) { kafkaProcessor1.startConsuming() }
        verify(exactly = 1) { kafkaProcessor2.startConsuming() }
    }
}