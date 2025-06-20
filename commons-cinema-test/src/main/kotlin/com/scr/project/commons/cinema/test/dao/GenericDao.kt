package com.scr.project.commons.cinema.test.dao

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.findOneById
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
abstract class GenericDao<T : Any>(
    private val mongoUri: String,
    private val entityClass: Class<T>,
    private val collectionName: String
) {

    private val logger = LoggerFactory.getLogger(GenericDao::class.java)
    private val client: MongoClient = KMongo.createClient(mongoUri)
    private val database: MongoDatabase = client.getDatabase("test")
    protected val collection: MongoCollection<T> = database.getCollection(collectionName, entityClass)

    init {
        logger.info("MongoClient initialized with URI: $mongoUri")
        logger.info("MongoCollection initialized with name: $collectionName and entity class: ${entityClass.simpleName}")
    }

    fun insert(entity: T) = collection.insertOne(entity)

    fun insertAll(entities: List<T>) = entities.forEach { insert(it) }

    fun findById(id: ObjectId): T? {
        return collection.findOneById(id)
    }

    fun findAll(): List<T> {
        return collection.find().toList()
    }

    fun findAllBy(predicate: Predicate<T>): List<T> {
        return collection.find().filter { predicate.test(it) }.toList()
    }

    fun findAnyBy(predicate: Predicate<T>): T? {
        return collection.find().filter { predicate.test(it) }.runCatching { random() }
            .getOrElse { throw Exception("No test data matching the predicate") }
    }

    fun findAny(): T? {
        return findAnyBy { true }
    }

    fun count() = collection.countDocuments()

    fun deleteAll() = collection.deleteMany()

    open fun defaultEntities() = emptyList<T>()

    fun initTestData() {
        deleteAll()
        insertAll(defaultEntities())
    }
}