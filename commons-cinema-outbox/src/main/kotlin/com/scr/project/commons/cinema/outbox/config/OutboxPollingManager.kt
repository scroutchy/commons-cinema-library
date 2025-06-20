package com.scr.project.commons.cinema.outbox.config

import com.scr.project.commons.cinema.outbox.config.Properties.SPRING_KAFKA_ENABLED
import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.service.OutboxRelayerService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import java.time.Duration

@Component
@ConditionalOnProperty(name = [SPRING_KAFKA_ENABLED], havingValue = "true", matchIfMissing = false)
class OutboxPollingManager(
    private val outboxRelayerService: OutboxRelayerService
) {

    private val logger: Logger = LoggerFactory.getLogger(OutboxPollingManager::class.java)
    private var pollingDisposable: Disposable? = null

    /**
     * Démarre le polling périodique pour traiter les événements outbox.
     */
    @PostConstruct
    fun startPolling() {
        pollingDisposable?.dispose()
        pollingDisposable = pollAndProcess().subscribe(
            {},
            { error -> logger.error("Polling error: ", error) }
        )
        logger.info("Outbox polling started.")
    }

    /**
     * Arrête le polling en cours.
     */
    @PreDestroy
    fun stopPolling() {
        pollingDisposable?.dispose()
        logger.info("Outbox polling stopped.")
    }

    private fun pollAndProcess(pollInterval: Duration = Duration.ofSeconds(1)): Flux<Outbox> {
        return Flux.interval(pollInterval)
            .doOnSubscribe { logger.debug("Polling interval initiated.") }
            .doOnNext { logger.trace("Polling tick: $it") }
            .flatMap {
                logger.debug("Fetching pending outbox events...")
                outboxRelayerService.processOutbox()
            }
            .doOnError { e -> logger.warn("Error during pollAndProcess flatMap operation: ${e.message}", e) }
    }
}

