package com.scr.project.commons.cinema.outbox.service

import com.scr.project.commons.cinema.outbox.config.Properties.SPRING_KAFKA_ENABLED
import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.repository.SimpleOutboxRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@ConditionalOnProperty(name = [SPRING_KAFKA_ENABLED], havingValue = "true", matchIfMissing = false)
class OutboxService(private val simpleOutboxRepository: SimpleOutboxRepository) : IOutboxService {

    private val logger: Logger = LoggerFactory.getLogger(OutboxService::class.java)

    override fun send(outbox: Outbox): Mono<Outbox> {
        return simpleOutboxRepository.insert(outbox)
            .doOnSubscribe { logger.debug("Persisting outbox record: ${outbox.payload}") }
            .doOnSuccess { logger.info("Persisted outbox record: ${outbox.payload}") }
            .doOnError { logger.warn("Error persisting outbox record: ${outbox.payload}") }
    }
}