package com.scr.project.commons.cinema.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.scr.project.commons.cinema.outbox.error.OutboxException.OnFailedOutboxDeletionException
import com.scr.project.commons.cinema.outbox.error.OutboxException.OnFailedProducerRecordCreationException
import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus.ERROR
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus.PENDING
import com.scr.project.commons.cinema.outbox.model.entity.OutboxStatus.PROCESSING
import com.scr.project.commons.cinema.outbox.repository.OutboxRepository
import com.scr.project.commons.cinema.outbox.repository.SimpleOutboxRepository
import org.apache.kafka.clients.producer.ProducerRecord
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono

@Service
class OutboxRelayerService(
    private val simpleOutboxRepository: SimpleOutboxRepository,
    private val outboxRepository: OutboxRepository,
    private val kafkaSender: KafkaSender<String, Any>,
    private val objectMapper: ObjectMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(OutboxRelayerService::class.java)

    fun processOutbox(): Flux<Outbox> {
        return simpleOutboxRepository.findAllByStatus(PENDING)
            .flatMap {
                logger.debug("Processing outbox event: {}", it.id)
                outboxRepository.updateStatus(it.id, PROCESSING)
                    .onErrorResume { e ->
                        logger.warn("Failed to update to PROCESSING status for outbox event ${it.id}: ${e.message}")
                        it.toMono()
                    }
                    .filter { it.status == PROCESSING }
                    .flatMap { processSingleOutboxEvent(it) }
                    .onErrorResume { e ->
                        when (e) {
                            is OnFailedOutboxDeletionException -> {
                                logger.warn("Attention required for outbox record with id ${it.id}. It was sent but not deleted.")
                                outboxRepository.updateStatus(it.id, ERROR)
                            }

                            is OnFailedProducerRecordCreationException -> {
                                logger.warn("Failed to create producer record for outbox event ${it.id}: ${e.message}")
                                outboxRepository.updateStatus(it.id, ERROR)
                            }

                            else -> {
                                logger.warn("Error processing outbox event ${it.id}: ${e.message}")
                                outboxRepository.updateStatus(it.id, PENDING)
                            }
                        }
                    }
            }
    }

    private fun processSingleOutboxEvent(outbox: Outbox): Mono<Outbox> {
        return outbox.toMono().delayUntil {
            createSenderRecord(it)
                .flatMap { kafkaSender.send(it.toMono()).single() }
                .doOnSuccess {
                    logger.info(
                        "Outbox event {} successfully sent to Kafka (Offset: {}, Partition: {}).",
                        outbox.id, it?.recordMetadata()?.offset(), it?.recordMetadata()?.partition()
                    )
                }
                .doOnError { e -> logger.warn("Failed to send outbox event {} to Kafka: {}", outbox.id, e.message, e) }
                .flatMap { deleteOutboxRecord(outbox) }
        }
    }

    private fun createSenderRecord(outbox: Outbox): Mono<SenderRecord<String, Any, ObjectId>> {
        return outbox.toMono()
            .map { ProducerRecord(it.topic, it.aggregateId, objectMapper.readValue(it.payload, Class.forName(it.aggregateType))) }
            .onErrorMap { e -> OnFailedProducerRecordCreationException(outbox.id.toHexString(), e) }
            .map { SenderRecord.create(it, outbox.id) }
    }

    private fun deleteOutboxRecord(outbox: Outbox): Mono<Void> {
        return simpleOutboxRepository.delete(outbox)
            .doOnSubscribe { logger.debug("Deleting outbox event {} after successful sending.", outbox.id) }
            .doOnSuccess { logger.debug("Outbox event {} successfully deleted.", outbox.id) }
            .doOnError { e -> logger.warn("Failed to delete outbox event {}: {}", outbox.id, e.message) }
            .onErrorMap { OnFailedOutboxDeletionException(outbox.id.toHexString()) }
    }
}
