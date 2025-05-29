package com.scr.project.commons.cinema.outbox.repository

import com.scr.project.commons.cinema.outbox.config.Properties.KAFKA_ENABLING_PROPERTY
import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import reactor.core.publisher.Mono

@ConditionalOnProperty(name = [KAFKA_ENABLING_PROPERTY], havingValue = "true", matchIfMissing = false)
fun interface OutboxRepository {

    fun updateStatus(id: ObjectId, status: OutboxStatus): Mono<Outbox>
}