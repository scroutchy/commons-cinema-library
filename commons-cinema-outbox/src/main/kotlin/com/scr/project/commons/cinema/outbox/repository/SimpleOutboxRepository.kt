package com.scr.project.commons.cinema.outbox.repository

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SimpleOutboxRepository : ReactiveMongoRepository<Outbox, ObjectId> {

    fun findAllByStatus(status: OutboxStatus): Flux<Outbox>
}