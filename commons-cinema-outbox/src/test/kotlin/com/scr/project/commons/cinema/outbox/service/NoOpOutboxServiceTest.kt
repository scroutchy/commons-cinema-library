package com.scr.project.commons.cinema.outbox.service

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.kotlin.test.test

class NoOpOutboxServiceTest {

    private val noOpOutboxService = NoOpOutboxService()

    @Test
    fun `send should succeed`() {
        val outbox = Outbox("type", "id", "{\"key\": \"value\"}", "topic")
        noOpOutboxService.send(outbox)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull
                assertThat(it.aggregateType).isEqualTo(outbox.aggregateType)
                assertThat(it.aggregateId).isEqualTo(outbox.aggregateId)
                assertThat(it.payload).isEqualTo(outbox.payload)
                assertThat(it.topic).isEqualTo(outbox.topic)
            }.verifyComplete()
    }
}