package com.scr.project.commons.cinema.outbox.service

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import reactor.core.publisher.Mono

fun interface IOutboxService {

    fun send(outbox: Outbox): Mono<Outbox>
}