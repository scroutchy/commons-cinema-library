package com.scr.project.commons.cinema.model.entity

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class TestMongoEntity(
    val name: String,
    @field:Id @BsonId val id: ObjectId? = null,
) : Auditable()