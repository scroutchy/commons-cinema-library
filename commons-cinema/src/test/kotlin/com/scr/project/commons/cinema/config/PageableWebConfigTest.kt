package com.scr.project.commons.cinema.config

import org.junit.jupiter.api.Test
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

class PageableWebConfigTest {

    private val pageableWebConfig = PageableWebConfig()

    @Test
    fun `configureArgumentResolvers should add ReactivePageableHandlerMethodArgumentResolver`() {
        val configurer = ArgumentResolverConfigurer()
        pageableWebConfig.configureArgumentResolvers(configurer)
        val resolvers = configurer.getCustomResolvers()
        assert(resolvers.any { it is ReactivePageableHandlerMethodArgumentResolver })
    }

    private fun ArgumentResolverConfigurer.getCustomResolvers(): List<Any?> {
        val field = ArgumentResolverConfigurer::class.java.getDeclaredField("customResolvers")
        field.isAccessible = true
        return field.get(this) as List<Any?>
    }
}