package com.scr.project.commons.cinema.model.entity

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TestMongoEntityRepository : ReactiveMongoRepository<TestMongoEntity, ObjectId>