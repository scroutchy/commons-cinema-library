package com.scr.project.commons.cinema.model.entity

import com.scr.project.commons.cinema.TestMongoApplication
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import reactor.kotlin.test.test
import java.time.temporal.ChronoUnit.SECONDS

@SpringBootTest
internal class MongoAuditableTest(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
    @Autowired private val testEntityRepository: TestMongoEntityRepository,
) : TestMongoApplication() {

    @BeforeEach
    fun setUp() {
        mongoTemplate.remove<TestMongoEntity>().all().subscribe()
    }

    @Test
    fun `should save entity with createdAt and lastModifiedAt`() {
        val newTestEntity = TestMongoEntity(name = "Test Entity save")
        testEntityRepository.save(newTestEntity).test()
            .expectSubscription()
            .consumeNextWith { s ->
                assertThat(s.name).isEqualTo(newTestEntity.name)
                assertThat(s.id).isNotNull
            }
            .verifyComplete()
        mongoTemplate.findOne(Query(), TestMongoEntity::class.java).test()
            .expectSubscription()
            .consumeNextWith { s ->
                assertThat(s.name).isEqualTo(newTestEntity.name)
                assertThat(s.id).isNotNull
                assertThat(s.createdAt).isNotNull.isCloseToUtcNow(TemporalUnitWithinOffset(5, SECONDS))
                assertThat(s.lastModifiedAt).isNotNull.isCloseToUtcNow(TemporalUnitWithinOffset(5, SECONDS))
            }.verifyComplete()
    }

    @Test
    fun `should insert entity with createdAt and lastModifiedAt`() {
        val newTestEntity = TestMongoEntity(name = "Test Entity insert")
        testEntityRepository.insert(newTestEntity).test()
            .expectSubscription()
            .consumeNextWith { s ->
                assertThat(s.name).isEqualTo(newTestEntity.name)
                assertThat(s.id).isNotNull
            }
            .verifyComplete()
        mongoTemplate.findOne(Query(), TestMongoEntity::class.java).test()
            .expectSubscription()
            .consumeNextWith { s ->
                assertThat(s.name).isEqualTo(newTestEntity.name)
                assertThat(s.id).isNotNull
                assertThat(s.createdAt).isNotNull.isCloseToUtcNow(TemporalUnitWithinOffset(5, SECONDS))
                assertThat(s.lastModifiedAt).isNotNull.isCloseToUtcNow(TemporalUnitWithinOffset(5, SECONDS))
            }.verifyComplete()
    }
}