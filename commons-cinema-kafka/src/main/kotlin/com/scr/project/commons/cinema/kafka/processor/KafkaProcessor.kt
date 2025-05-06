package com.scr.project.commons.cinema.kafka.processor

import reactor.core.publisher.Flux
import reactor.kafka.receiver.ReceiverRecord

fun interface KafkaProcessor<T> {

    fun startConsuming(): Flux<ReceiverRecord<String, T>>
}