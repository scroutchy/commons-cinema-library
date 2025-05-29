package com.scr.project.commons.cinema.outbox.repository

import com.scr.project.commons.cinema.outbox.config.Properties.SPRING_KAFKA_ENABLED
import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import reactor.core.publisher.Mono

@ConditionalOnProperty(name = [SPRING_KAFKA_ENABLED], havingValue = "true", matchIfMissing = false)
fun interface OutboxRepository {

    fun updateStatus(id: ObjectId, status: OutboxStatus): Mono<Outbox>
}