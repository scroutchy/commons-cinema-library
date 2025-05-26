package com.scr.project.commons.cinema.outbox.error

import com.scr.project.commons.cinema.outbox.error.OutboxException.OnFailedOutboxDeletionException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OutboxExceptionTest {

    @Test
    fun `OnFailedOutboxDeletionException should return expected error message`() {
        val outboxId = "outboxId"
        val exception = OnFailedOutboxDeletionException(outboxId)
        assertThat(exception.message).isEqualTo("Failed to delete outbox with id $outboxId")
    }

    @Test
    fun `OnFailedProducerRecordCreationException should return expected error message`() {
        val outboxId = "outboxId"
        val exception = OutboxException.OnFailedProducerRecordCreationException(outboxId)
        assertThat(exception.message).isEqualTo("Failed to create producer record for outbox with id $outboxId")
    }
}