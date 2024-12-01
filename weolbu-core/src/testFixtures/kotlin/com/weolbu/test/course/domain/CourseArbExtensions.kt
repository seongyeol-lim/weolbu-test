package com.weolbu.test.course.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.ArbitraryBuilder
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.instant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import java.time.Duration
import java.time.Instant

fun Arb.Companion.courseTitle(): Arb<String> {
    val list = listOf("너나위의 내집마련 기초반", "신도시투자 기초반", "열반스쿨 기초반", "열반스쿨 중급반", "지방투자 기초반")
    return Arb.of(list).map { "$it-${(0..9).random()}" }
}

fun Arb.Companion.course(
    arbId: Arb<Long> = Arb.long(0..Long.MAX_VALUE),
    arbTitle: Arb<String> = courseTitle(),
    arbMaxParticipants: Arb<Int> = Arb.int(5..1000),
    arbPrice: Arb<Int> = Arb.int(1000..500000),
    arbCreatedAt: Arb<Instant> = Arb.instant(
        minValue = Instant.now().minus(Duration.ofDays(365)),
        maxValue = Instant.now(),
    ),
    arbCurrentParticipants: Arb<Int> = Arb.constant(0),
): Arb<Course> {
    return Arb.bind(
        arbId,
        arbTitle,
        arbMaxParticipants,
        arbPrice,
        arbCreatedAt,
        arbCurrentParticipants,
    ) { id: Long, title: String, maxParticipants: Int, price: Int, createdAt: Instant, currentParticipants: Int ->
        Course(
            id = id,
            title = title,
            maxParticipants = maxParticipants,
            price = price,
            createdAt = createdAt,
            currentParticipants = currentParticipants,
        )
    }
}

fun Arb.Companion.sequence(start: Long = 0): Arb<Long> {
    val iter = generateSequence(start) { it + 1 }.iterator()
    return ArbitraryBuilder.create {
        iter.next()
    }.build()
}
