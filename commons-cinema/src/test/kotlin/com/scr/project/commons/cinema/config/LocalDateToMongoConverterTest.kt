package com.scr.project.commons.cinema.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date

class LocalDateToMongoConverterTest {

    private val converter = LocalDateToMongoConverter()

    @Test
    fun `convert should convert LocalDate to Date`() {
        val localDate = LocalDate.of(2023, 1, 1)
        val expectedDate = Date(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        val result = converter.convert(localDate)

        assertThat(result).isEqualTo(expectedDate)
    }
}