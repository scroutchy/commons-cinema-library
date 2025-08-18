package com.scr.project.commons.cinema.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.Optional

@Configuration(proxyBeanMethods = false)
@EnableReactiveMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
@EnableReactiveMongoRepositories(basePackages = ["com.scr.project"])
@EnableTransactionManagement
class MongoCoreConfiguration {

    @Bean
    fun customConversions() = MongoCustomConversions(
        listOf(LocalDateToMongoConverter(), MongoToLocalDateConverter())
    )

    @Bean
    fun reactiveMongoTransactionManager(factory: ReactiveMongoDatabaseFactory) = ReactiveMongoTransactionManager(factory)

    @Bean
    fun transactionalOperator(reactiveTransactionManager: ReactiveTransactionManager) =
        TransactionalOperator.create(reactiveTransactionManager)

    @Bean
    fun dateTimeProvider() = DateTimeProvider { Optional.of(LocalDateTime.now(UTC)) }
}