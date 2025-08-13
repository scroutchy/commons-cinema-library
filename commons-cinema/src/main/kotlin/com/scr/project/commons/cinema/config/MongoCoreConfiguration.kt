package com.scr.project.commons.cinema.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.Optional

@Configuration(proxyBeanMethods = false)
@EnableReactiveMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
class MongoCoreConfiguration {

    @Bean
    fun dateTimeProvider() = DateTimeProvider { Optional.of(LocalDateTime.now(UTC)) }
}