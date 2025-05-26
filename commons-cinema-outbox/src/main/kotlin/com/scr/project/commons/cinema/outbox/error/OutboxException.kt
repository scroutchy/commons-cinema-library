package com.scr.project.commons.cinema.outbox.error

sealed class OutboxException : RuntimeException() {

    class OnFailedOutboxDeletionException(outboxId: String) : OutboxException() {

        override val message = "Failed to delete outbox with id $outboxId"
    }

    class OnFailedProducerRecordCreationException(
        outboxId: String,
        override val cause: Throwable? = null
    ) : OutboxException() {

        override val message = "Failed to create producer record for outbox with id $outboxId"
    }
}