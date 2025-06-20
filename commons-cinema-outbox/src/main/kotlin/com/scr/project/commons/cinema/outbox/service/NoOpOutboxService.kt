package com.scr.project.commons.cinema.outbox.service

import com.scr.project.commons.cinema.outbox.config.Properties.SPRING_KAFKA_ENABLED
import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
@ConditionalOnProperty(name = [SPRING_KAFKA_ENABLED], havingValue = "false", matchIfMissing = true)
class NoOpOutboxService : IOutboxService {

    private val logger: Logger = LoggerFactory.getLogger(NoOpOutboxService::class.java)

    override fun send(outbox: Outbox): Mono<Outbox> {
        return outbox.toMono()
            .doOnSubscribe { logger.debug("Kafka deactivated, no message sent") }
    }
}