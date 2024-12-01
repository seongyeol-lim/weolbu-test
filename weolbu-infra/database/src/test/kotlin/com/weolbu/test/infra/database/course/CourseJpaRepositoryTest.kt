package com.weolbu.test.infra.database.course

import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.course.domain.courseTitle
import com.weolbu.test.infra.database.WeolbuDataSource
import com.weolbu.test.infra.database.WeolbuJpaConfiguration
import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest
import com.weolbu.test.user.domain.userAccountId
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSortedDescendingBy
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.instant
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.take
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@SpringBootTest(
    classes = [WeolbuJpaConfiguration::class],
    properties = [
        "spring.config.import=classpath:/config-datasource.yml",
        "logging.level.root=INFO",
    ],
)
class CourseJpaRepositoryTest(
    private val courseRepository: CourseJpaRepository,
    private val courseRegistrationRepository: CourseRegistrationJpaRepository,
) : FunSpec({
    test("CRD Test") {
        val newEntity = courseRepository.save(
            CourseEntity(
                id = null,
                title = Arb.courseTitle().single(),
                maxParticipants = Arb.long(5L..100).single(),
                price = Arb.long(1000L..90000).single(),
                createdAt = LocalDateTime.now(),
            ),
        )

        val newEntityId: Long = newEntity.id!!

        courseRepository.findById(newEntityId).getOrNull().shouldNotBeNull {
            id shouldBe newEntityId
        }

        courseRepository.deleteById(newEntityId)

        courseRepository.findById(newEntityId).isEmpty shouldBe true
    }

    context("getAllCourses() Test") {
        // 강의 25개 등록
        val courseEntities: List<CourseEntity> = courseRepository.saveAll(
            Arb.courseEntity().take(25).toList(),
        )

        // 임의로 10개 강의에 대해서 강의 등록
        for (course in courseEntities.shuffled().take(10)) {
            val courseId: Long = course.id!!

            // 임의의 등록자 수가 강의 등록
            repeat((1..course.maxParticipants.toInt()).random()) {
                val userAccountId = Arb.userAccountId().single()
                val entity = createCourseRegistration(courseId = courseId, userAccountId = userAccountId)
                courseRegistrationRepository.save(entity)
            }
        }

        // pageSize 를 course 보다 1개 많게 설정하여 정렬 체크
        val pageRequest = OffsetPageRequest.of(pageNum = 1, pageSize = courseEntities.size + 1).getOrThrow()

        test("sort - CourseSort.RECENTLY_REGISTERED") {
            val sort = CourseSort.RECENTLY_REGISTERED
            val result: OffsetPageContent<CourseAndRegistrationEntity> =
                courseRepository.getAllCourses(pageRequest, sort)

            println(result.items.joinToString("\n") { it.toSummary() })
            println()

            withClue("강의 최근 등록순") {
                result.totalElements shouldBe courseEntities.size
                result.items.size shouldBe courseEntities.size
                result.items.shouldBeSortedDescendingBy { it.courseEntity.createdAt }
            }
        }

        test("sort - CourseSort.MOST_APPLICANTS") {
            val sort = CourseSort.MOST_APPLICANTS
            val result: OffsetPageContent<CourseAndRegistrationEntity> =
                courseRepository.getAllCourses(pageRequest, sort)

            println(result.items.joinToString("\n") { it.toSummary() })
            println()

            withClue("강의 신청자 많은 순") {
                result.totalElements shouldBe courseEntities.size
                result.items.size shouldBe courseEntities.size
                result.items.shouldBeSortedDescendingBy { it.currentParticipants }
            }
        }

        test("sort - CourseSort.HIGHEST_APPLICATION_RATE") {
            val sort = CourseSort.HIGHEST_APPLICATION_RATE
            val result: OffsetPageContent<CourseAndRegistrationEntity> =
                courseRepository.getAllCourses(pageRequest, sort)

            println(result.items.joinToString("\n") { it.toSummary() })
            println()

            withClue("강의 신청률 높은 순") {
                result.totalElements shouldBe courseEntities.size
                result.items.size shouldBe courseEntities.size
                result.items.shouldBeSortedDescendingBy { it.registrationRate }
            }
        }

        test("Pagination 범위가 벗어난 경우") {
            val result: OffsetPageContent<CourseAndRegistrationEntity> =
                courseRepository.getAllCourses(
                    pageRequest = OffsetPageRequest.of(pageNum = 999, pageSize = 10).getOrThrow(),
                    sort = CourseSort.MOST_APPLICANTS,
                )

            println(result.items.joinToString("\n") { it.toSummary() })
            println()

            withClue("empty list 응답") {
                result.totalElements shouldBe courseEntities.size
                result.items.size shouldBe 0
            }
        }
    }
})

private fun CourseAndRegistrationEntity.toSummary(): String {
    return "- courseId=${this.courseEntity.id}, createdAt=${this.courseEntity.createdAt}, currentParticipants=${this.currentParticipants}, maxParticipants=${this.courseEntity.maxParticipants} registrationRate=${this.registrationRate}"
}

private fun Arb.Companion.courseEntity(
    arbTitle: Arb<String> = courseTitle(),
    arbMaxParticipants: Arb<Long> = Arb.long(5L..1000),
    arbPrice: Arb<Long> = Arb.long(1000L..500000),
    arbCreatedAt: Arb<Instant> = Arb.instant(
        minValue = Instant.now().minus(Duration.ofDays(365)),
        maxValue = Instant.now(),
    ),
): Arb<CourseEntity> {
    return Arb.bind(
        arbTitle,
        arbMaxParticipants,
        arbPrice,
        arbCreatedAt,
    ) { title: String, maxParticipants: Long, price: Long, createdAt: Instant ->
        CourseEntity(
            id = null,
            title = title,
            maxParticipants = maxParticipants,
            price = price,
            createdAt = LocalDateTime.ofInstant(createdAt, WeolbuDataSource.ZONE_OFFSET),
        )
    }
}

private fun createCourseRegistration(courseId: Long, userAccountId: Long) =
    CourseRegistrationEntity(
        id = null,
        userAccountId = userAccountId,
        courseId = courseId,
        createdAt = LocalDateTime.now(),
    )
