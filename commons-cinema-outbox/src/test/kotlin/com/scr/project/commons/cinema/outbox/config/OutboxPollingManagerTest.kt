package com.scr.project.commons.cinema.outbox.config

import com.scr.project.commons.cinema.outbox.service.OutboxRelayerService
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.scheduler.VirtualTimeScheduler
import java.time.Duration

class OutboxPollingManagerTest {

    private val outboxRelayerService = mockk<OutboxRelayerService>()
    private val pollingManager = OutboxPollingManager(outboxRelayerService)

    @BeforeEach
    fun setUp() {
        clearMocks(outboxRelayerService)
        every { outboxRelayerService.processOutbox() } returns Flux.empty()
    }

    @Test
    fun `startPolling must call processOutbox`() {
        VirtualTimeScheduler.getOrSet()
        pollingManager.startPolling()
        VirtualTimeScheduler.getOrSet().advanceTimeBy(Duration.ofSeconds(1))
        verify(atLeast = 1) { outboxRelayerService.processOutbox() }
        pollingManager.stopPolling()
    }

    @Test
    fun `startPolling must call processOutbox periodically`() {
        VirtualTimeScheduler.getOrSet()
        pollingManager.startPolling()
        VirtualTimeScheduler.getOrSet().advanceTimeBy(Duration.ofSeconds(3))
        verify(atLeast = 2) { outboxRelayerService.processOutbox() }
        pollingManager.stopPolling()
    }

    @Test
    fun `stopPolling must stop polling without error`() {
        pollingManager.startPolling()
        pollingManager.stopPolling()
    }
}

