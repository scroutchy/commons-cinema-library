package com.scr.project.commons.cinema.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@Configuration(proxyBeanMethods = false)
@EnableReactiveMongoAuditing
class MongoAuditingConfig {
}