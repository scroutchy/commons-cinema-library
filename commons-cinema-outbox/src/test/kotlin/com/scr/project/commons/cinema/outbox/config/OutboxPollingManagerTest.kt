package com.scr.project.commons.cinema.outbox.config

import com.scr.project.commons.cinema.outbox.service.OutboxRelayerService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux

class OutboxPollingManagerTest {

    private val outboxRelayerService = mockk<OutboxRelayerService>()
    private val pollingManager = OutboxPollingManager(outboxRelayerService)

    @Test
    fun `startPolling must call processOutbox`() {
        every { outboxRelayerService.processOutbox() } returns Flux.empty()
        pollingManager.startPolling()
        Thread.sleep(1100) // attendre un peu plus d'une période
        verify(atLeast = 1) { outboxRelayerService.processOutbox() }
        pollingManager.stopPolling()
    }

    @Test
    fun `startPolling must call processOutbox periodically`() {
        every { outboxRelayerService.processOutbox() } returns Flux.empty()
        pollingManager.startPolling()
        Thread.sleep(3300) // attendre un peu plus d'une période
        verify(atLeast = 2) { outboxRelayerService.processOutbox() }
        pollingManager.stopPolling()
    }

    @Test
    fun `stopPolling doit arrêter le polling sans erreur`() {
        every { outboxRelayerService.processOutbox() } returns Flux.empty()
        pollingManager.startPolling()
        pollingManager.stopPolling()
    }
}

