package com.scr.project.commons.cinema.outbox.service

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.repository.SimpleOutboxRepository
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

class OutboxServiceTest {

    private val simpleOutboxRepository = mockk<SimpleOutboxRepository>()
    private val outboxService = OutboxService(simpleOutboxRepository)

    @BeforeEach
    fun setUp() {
        clearMocks(simpleOutboxRepository)
    }

    @Test
    fun `send should succeed`() {
        val outbox = Outbox("type", "id", "{\"key\": \"value\"}", "topic")
        every { simpleOutboxRepository.insert(any<Outbox>()) } answers { outbox.toMono() }
        outboxService.send(outbox)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull
                assertThat(it.aggregateType).isEqualTo(outbox.aggregateType)
                assertThat(it.aggregateId).isEqualTo(outbox.aggregateId)
                assertThat(it.payload).isEqualTo(outbox.payload)
                assertThat(it.topic).isEqualTo(outbox.topic)
            }.verifyComplete()
        verify(exactly = 1) { simpleOutboxRepository.insert(outbox) }
        confirmVerified(simpleOutboxRepository)
    }
}