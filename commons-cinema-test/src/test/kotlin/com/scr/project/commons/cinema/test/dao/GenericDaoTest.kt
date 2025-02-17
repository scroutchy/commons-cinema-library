package com.scr.project.commons.cinema.test.dao

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers

@ContextConfiguration
@ExtendWith(SpringExtension::class)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenericDaoIntegrationTest {

    companion object {

        val mongoContainer = MongoDBContainer("mongo:6.0").apply { start() }
    }

    data class TestEntity(@BsonId val id: ObjectId = ObjectId.get(), val name: String)

    @Repository
    class TestEntityDao(@Value("\${spring.data.mongodb.uri}") private val mongoUri: String) :
        GenericDao<TestEntity>(mongoUri, TestEntity::class.java, "testEntities") {

        override fun defaultEntities() = listOf(TestEntity(name = "default"))
    }

    private lateinit var dao: TestEntityDao

    @BeforeEach
    fun setUp() {
        val mongoUri = mongoContainer.replicaSetUrl
        dao = TestEntityDao(mongoUri)
        dao.initTestData()
    }

    @Test
    fun `should insert and retrieve entity`() {
        val entity = TestEntity(name = "test")
        dao.insert(entity)
        val retrieved = dao.findById(entity.id)
        assertThat(retrieved).isNotNull
        assertThat(retrieved?.name).isEqualTo("test")
    }

    @Test
    fun `should insert all and retrieve entities`() {
        val entity1 = TestEntity(name = "test")
        val entity2 = TestEntity(name = "test2")
        dao.insertAll(listOf(entity1, entity2))
        val retrieved1 = dao.findAnyBy { it.id == entity1.id }
        assertThat(retrieved1).isNotNull
        assertThat(retrieved1?.name).isEqualTo("test")
        val retrieved2 = dao.findAllBy { it.id == entity2.id }.first()
        assertThat(retrieved2).isNotNull
        assertThat(retrieved2.name).isEqualTo("test2")
        val randomlyRetrieved = dao.findAny()
        assertThat(randomlyRetrieved).isNotNull

        assertThatThrownBy { dao.findAnyBy { it.id == ObjectId.get() } }
            .isInstanceOf(Exception::class.java)
            .hasMessage("No test data matching the predicate")
    }

    @Test
    fun `should return default entity after init`() {
        val entities = dao.findAll()
        assertEquals(1, entities.size)
        assertEquals("default", entities[0].name)
    }

    @Test
    fun `should delete all entities`() {
        dao.deleteAll()
        assertEquals(0, dao.count())
    }
}
