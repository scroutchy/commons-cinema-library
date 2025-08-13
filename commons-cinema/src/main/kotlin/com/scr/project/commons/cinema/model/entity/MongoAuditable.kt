package com.scr.project.commons.cinema.model.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

abstract class MongoAuditable {

    @CreatedDate
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var lastModifiedAt: LocalDateTime? = null
}