package com.scr.project.commons.cinema.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.scr.project.com.scr.project.commons.cinema.config.MongoToLocalDateConverter
import java.time.Instant
import java.time.LocalDate
import java.util.Date

class MongoToLocalDateConverterTest {

    private val converter = MongoToLocalDateConverter()

    @Test
    fun `convert should convert Date to LocalDate`() {
        val date = Date.from(Instant.now())
        assertThat(converter.convert(date)).isEqualTo(LocalDate.now())

    }
}