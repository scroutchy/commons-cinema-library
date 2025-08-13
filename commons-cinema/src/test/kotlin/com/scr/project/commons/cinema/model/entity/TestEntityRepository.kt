package com.scr.project.commons.cinema.model.entity

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TestEntityRepository : ReactiveMongoRepository<TestEntity, String>