package com.scr.project.commons.cinema.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus.ERROR
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus.PENDING
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus.PROCESSING
import com.scr.project.commons.cinema.outbox.repository.OutboxRepository
import com.scr.project.commons.cinema.outbox.repository.SimpleOutboxRepository
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

class OutboxRelayerServiceTest {

    private val simpleOutboxRepository = mockk<SimpleOutboxRepository>()
    private val outboxRepository = mockk<OutboxRepository>()
    private val kafkaSender = mockk<KafkaSender<String, SpecificRecord>>()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    private val outboxRelayerService = OutboxRelayerService(simpleOutboxRepository, outboxRepository, kafkaSender, objectMapper)
    private val outbox = Outbox(DummyKafkaDto::class.java.name, "id", "{ \"id\": \"value\", \"type\": \"ACTOR\" }", "topic")
    private val senderRecordSlot = slot<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()

    data class DummyKafkaDto(val id: String, val type: String) : SpecificRecord by mockk()

    @BeforeEach
    fun setUp() {
        clearMocks(simpleOutboxRepository, outboxRepository, kafkaSender)
        every { kafkaSender.send(capture(senderRecordSlot)) } answers {
            var correlationId: ObjectId? = null
            senderRecordSlot.captured.subscribe { r ->
                correlationId = r.correlationMetadata()
            }
            val mockRecordMetadata = mockk<RecordMetadata>()
            every { mockRecordMetadata.offset() } returns 123L // Example offset
            every { mockRecordMetadata.partition() } returns 456
            val mockSenderResult = mockk<SenderResult<ObjectId>>()
            every { mockSenderResult.correlationMetadata() } returns correlationId
            every { mockSenderResult.recordMetadata() } returns mockRecordMetadata
            every { mockSenderResult.exception() } returns null // Indicate no exception
            Flux.just(mockSenderResult)
        }
        every { outboxRepository.updateStatus(outbox.id, PROCESSING) } answers { outbox.copy(status = PROCESSING).toMono() }
        every { outboxRepository.updateStatus(outbox.id, PENDING) } answers { outbox.copy(status = PENDING).toMono() }
        every { outboxRepository.updateStatus(outbox.id, ERROR) } answers { outbox.copy(status = ERROR).toMono() }
    }

    @Test
    fun `processOutbox should succeed when outbox is empty`() {
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { Flux.empty() }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
        verify(exactly = 1) { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(inverse = true) { kafkaSender.send<Publisher<SenderRecord<String, DummyKafkaDto, String>>>(any()) }
        verify(inverse = true) { simpleOutboxRepository.delete(any()) }
        confirmVerified(simpleOutboxRepository, outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should succeed when outbox contains one document`() {
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(outbox).toFlux() }
        every { simpleOutboxRepository.delete(outbox.copy(status = PROCESSING)) } answers { Mono.empty() }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()
        verify(exactly = 1) { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox.id, PROCESSING) }
        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(exactly = 1) { simpleOutboxRepository.delete(outbox.copy(status = PROCESSING)) }
        confirmVerified(simpleOutboxRepository, outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should handle kafka exception, retry and not delete outbox record`() {
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(outbox).toFlux() }
        every { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) } answers {
            Flux.error(RuntimeException("Kafka send failed"))
        }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()
        verify(exactly = 1) { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox.id, PROCESSING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox.id, PENDING) }
        verify(inverse = true) { simpleOutboxRepository.delete(outbox) }
        verify(inverse = true) { simpleOutboxRepository.delete(outbox.copy(status = PROCESSING)) }
        confirmVerified(simpleOutboxRepository, outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should handle when deletion of outbox fails`() {
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(outbox).toFlux() }
        every { simpleOutboxRepository.delete(outbox.copy(status = PROCESSING)) } answers { Mono.error(RuntimeException("Delete failed")) }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()
        verify(exactly = 1) { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox.id, PROCESSING) }
        verify(inverse = true) { outboxRepository.updateStatus(outbox.id, PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox.id, ERROR) }
        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(exactly = 1) { simpleOutboxRepository.delete(outbox.copy(status = PROCESSING)) }
        confirmVerified(simpleOutboxRepository, outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should handle the case a status update to PROCESSING fails`() {
        val outbox1 = Outbox(DummyKafkaDto::class.java.name, "id", "{ \"id\": \"value1\", \"type\": \"MOVIE\" }", "topic")
        val outbox2 = Outbox(DummyKafkaDto::class.java.name, "id", "{ \"id\": \"value2\", \"type\": \"MOVIE\" }", "topic")
        val outbox3 = Outbox(DummyKafkaDto::class.java.name, "id", "{ \"id\": \"value3\", \"type\": \"MOVIE\" }", "topic")
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(outbox1, outbox2, outbox3).toFlux() }
        every { simpleOutboxRepository.delete(any<Outbox>()) } answers { Mono.empty() }
        every { outboxRepository.updateStatus(outbox1.id, PROCESSING) } answers { outbox1.copy(status = PROCESSING).toMono() }
        every {
            outboxRepository.updateStatus(
                outbox2.id,
                PROCESSING
            )
        } answers { Mono.error(RuntimeException("update status failure")) }//{ RuntimeException("update status failure").toMono() }
        every { outboxRepository.updateStatus(outbox2.id, ERROR) } answers { outbox2.copy(status = ERROR).toMono() }
        every { outboxRepository.updateStatus(outbox3.id, PROCESSING) } answers { outbox3.copy(status = PROCESSING).toMono() }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(2)
            .verifyComplete()
        verify(exactly = 1) { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox1.id, PROCESSING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox2.id, PROCESSING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox3.id, PROCESSING) }
        verify(exactly = 2) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(exactly = 1) { simpleOutboxRepository.delete(outbox1.copy(status = PROCESSING)) }
        verify(inverse = true) { simpleOutboxRepository.delete(outbox2.copy(status = PROCESSING)) }
        verify(exactly = 1) { simpleOutboxRepository.delete(outbox3.copy(status = PROCESSING)) }
    }

    @Test
    fun `processOutbox should succeed when more than one document in outbox and reject unknown types`() {
        val outbox1 = Outbox(DummyKafkaDto::class.java.name, "id", "{ \"id\": \"value1\", \"type\": \"MOVIE\" }", "topic")
        val outbox2 = Outbox("dummy", "id", "{ \"id\": \"value1\", \"surname\": \"surname\", \"name\": \"name\" }", "topic")
        val outbox3 = Outbox(DummyKafkaDto::class.java.name, "id", "{ \"id\": \"value3\", \"type\": \"MOVIE\" }", "topic")
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(outbox, outbox1, outbox2, outbox3).toFlux() }
        every { simpleOutboxRepository.delete(any<Outbox>()) } answers { Mono.empty() }
        every { outboxRepository.updateStatus(outbox1.id, PROCESSING) } answers { outbox1.copy(status = PROCESSING).toMono() }
        every { outboxRepository.updateStatus(outbox2.id, PROCESSING) } answers { outbox2.copy(status = PROCESSING).toMono() }
        every { outboxRepository.updateStatus(outbox3.id, PROCESSING) } answers { outbox3.copy(status = PROCESSING).toMono() }
        every { outboxRepository.updateStatus(outbox2.id, PENDING) } answers { outbox2.copy(status = PENDING).toMono() }
        every { outboxRepository.updateStatus(outbox2.id, ERROR) } answers { outbox2.copy(status = ERROR).toMono() }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(4)
            .verifyComplete()
        verify(exactly = 1) { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox.id, PROCESSING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox1.id, PROCESSING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox2.id, PROCESSING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox3.id, PROCESSING) }
        verify(inverse = true) { outboxRepository.updateStatus(outbox2.id, PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outbox2.id, ERROR) }
        verify(exactly = 3) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(exactly = 1) { simpleOutboxRepository.delete(outbox.copy(status = PROCESSING)) }
        verify(exactly = 1) { simpleOutboxRepository.delete(outbox1.copy(status = PROCESSING)) }
        verify(exactly = 1) { simpleOutboxRepository.delete(outbox3.copy(status = PROCESSING)) }
        verify(inverse = true) { simpleOutboxRepository.delete(outbox2.copy(status = PROCESSING)) }
        verify(inverse = true) { simpleOutboxRepository.delete(outbox2.copy(status = PENDING)) }
        confirmVerified(simpleOutboxRepository, outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should handle deserialization error in createSenderRecord`() {
        val invalidOutbox = outbox.copy(payload = "{ invalid json }", aggregateType = "com.scr.project.sam.UnknownType")
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(invalidOutbox).toFlux() }
        every { outboxRepository.updateStatus(invalidOutbox.id, PROCESSING) } answers { invalidOutbox.copy(status = PROCESSING).toMono() }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()
        verify { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(invalidOutbox.id, PROCESSING) }
        verify(inverse = true) { outboxRepository.updateStatus(invalidOutbox.id, PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(invalidOutbox.id, ERROR) }
        verify(inverse = true) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(inverse = true) { simpleOutboxRepository.delete(any()) }
    }

    @Test
    fun `processOutbox should handle error when updating status to PROCESSING`() {
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(outbox).toFlux() }
        every { outboxRepository.updateStatus(outbox.id, PROCESSING) } answers { Mono.error(RuntimeException("Update status failed")) }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
        verify { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify { outboxRepository.updateStatus(outbox.id, PROCESSING) }
        verify(inverse = true) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(inverse = true) { simpleOutboxRepository.delete(any()) }
    }

    @Test
    fun `processOutbox should handle error in findAllByStatus`() {
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { Flux.error(RuntimeException("DB error")) }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectError(RuntimeException::class.java)
            .verify()
        verify { simpleOutboxRepository.findAllByStatus(PENDING) }
    }

    @Test
    fun `processOutbox should handle deserialization error due to missing constructor`() {
        val outboxWithUnknownType = outbox.copy(aggregateType = "java.util.Locale")
        every { simpleOutboxRepository.findAllByStatus(PENDING) } answers { listOf(outboxWithUnknownType).toFlux() }
        every { outboxRepository.updateStatus(outboxWithUnknownType.id, PROCESSING) } answers {
            outboxWithUnknownType.copy(status = PROCESSING).toMono()
        }
        every { outboxRepository.updateStatus(outboxWithUnknownType.id, ERROR) } answers {
            outboxWithUnknownType.copy(status = ERROR).toMono()
        }
        outboxRelayerService.processOutbox()
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()
        verify { simpleOutboxRepository.findAllByStatus(PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outboxWithUnknownType.id, PROCESSING) }
        verify(inverse = true) { outboxRepository.updateStatus(outboxWithUnknownType.id, PENDING) }
        verify(exactly = 1) { outboxRepository.updateStatus(outboxWithUnknownType.id, ERROR) }
        verify(inverse = true) { kafkaSender.send(any<Mono<SenderRecord<String, SpecificRecord, ObjectId>>>()) }
        verify(inverse = true) { simpleOutboxRepository.delete(any()) }
    }
}

