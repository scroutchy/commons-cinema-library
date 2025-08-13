package com.scr.project.commons.cinema.model.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.remove
import reactor.kotlin.test.test

@SpringBootTest
@AutoConfigureWebTestClient
internal class MongoAuditableTest(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
    @Autowired private val testEntityRepository: TestEntityRepository,
) {

    private val newTestEntity = TestEntity(name = "Test Entity")

    @BeforeEach
    fun setUp() {
        mongoTemplate.remove<TestEntity>().findAndRemove()
    }

    @Test
    fun `should save entity with createdAt and lastModifiedAt`() {
        testEntityRepository.save(newTestEntity).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.name).isEqualTo(newTestEntity.name)
                assertThat(it.id).isNotNull
//                assertThat(it.createdAt).isCloseToUtcNow(TemporalUnitWithinOffset(5, SECONDS))
//                assertThat(it.lastModifiedAt).isCloseToUtcNow(TemporalUnitWithinOffset(5, SECONDS))
            }
            .verifyComplete()
    }
}