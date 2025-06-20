package com.scr.project.commons.cinema.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders.ACCEPT_RANGES
import org.springframework.http.HttpHeaders.CONTENT_RANGE
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.PARTIAL_CONTENT
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.test.test
import java.time.LocalDate

internal class RangedResponseTest {

    @Test
    fun `to ranged response should return accept range header with collection name inferred from ApiDTO`() {
        Flux.empty<GenericClassApiDto>().toRangedResponse(GenericClassApiDto::class.java)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.headers[ACCEPT_RANGES]!!.first()).isEqualTo("genericclasss")
                assertThat(it.statusCode).isEqualTo(OK)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response should return content range header with range value and ok status when returning all requested elements`() {
        genericDtos(5).toFlux().toRangedResponse(GenericClassApiDto::class.java, Pageable.ofSize(10))
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(5)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("genericclasss 0-4/5")
                assertThat(it.statusCode).isEqualTo(OK)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response should return content range header with range value and partial content status when returning all requested elements`() {
        genericDtos(5).toFlux().toRangedResponse(GenericClassApiDto::class.java, Pageable.ofSize(5)).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(5)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("genericclasss 0-4/5")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and PARTIAL_CONTENT status when returning less elements than the requested total count`() {
        genericDtos().toFlux().toRangedResponse(GenericClassApiDto::class.java, Pageable.ofSize(10)).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("genericclasss 0-9/10")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and partial content status when returning all elements equal to page size`() {
        genericDtos().toFlux().toRangedResponse(GenericClassApiDto::class.java, Pageable.ofSize(10)).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("genericclasss 0-9/10")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and partial content status when returning all elements equal to page size and total count is provided`() {
        genericDtos().toFlux().toRangedResponse(GenericClassApiDto::class.java, Pageable.ofSize(10), 30).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("genericclasss 0-9/10")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and OK status when returning all elements and total count is provided and smaller than page size`() {
        genericDtos().toFlux().toRangedResponse(GenericClassApiDto::class.java, Pageable.ofSize(10), 5).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("genericclasss 0-9/10")
                assertThat(it.statusCode).isEqualTo(OK)
            }
            .verifyComplete()
    }

    private fun genericDtos(number: Int = 10) = List(number) { GenericClassApiDto() }
}

data class GenericClassApiDto(
    val id: String = "id",
    val name: String = "name",
    val birthDate: LocalDate = LocalDate.now(),
    val birthPlace: String = "birthPlace",
)