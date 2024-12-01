package com.weolbu.test.adapter.course

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.weolbu.test.course.domain.Course
import com.weolbu.test.course.domain.CourseRepository
import com.weolbu.test.course.domain.CourseRepository.FailureType
import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.infra.database.WeolbuDataSource
import com.weolbu.test.infra.database.course.CourseAndRegistrationEntity
import com.weolbu.test.infra.database.course.CourseEntity
import com.weolbu.test.infra.database.course.CourseJpaRepository
import com.weolbu.test.infra.database.course.CourseRegistrationEntity
import com.weolbu.test.infra.database.course.CourseRegistrationJpaRepository
import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime

@Component
class CourseRepositoryAdapter(
    private val courseJpaRepository: CourseJpaRepository,
    private val registrationJpaRepository: CourseRegistrationJpaRepository,
) : CourseRepository {
    override fun getAllCourse(pageRequest: OffsetPageRequest, sort: CourseSort): OffsetPageContent<Course> {
        val result = courseJpaRepository.getAllCourses(pageRequest, sort)
        return OffsetPageContent(
            pageSize = result.pageSize,
            pageNum = result.pageNum,
            totalElements = result.totalElements,
            items = result.items.map { it.toDomainEntity() },
        )
    }

    override fun saveNewCourse(title: String, maxParticipants: Long, price: Long, createdAt: Instant) {
        val newEntity = CourseEntity(
            id = null,
            title = title,
            maxParticipants = maxParticipants,
            price = price,
            createdAt = LocalDateTime.ofInstant(createdAt, WeolbuDataSource.ZONE_OFFSET),
        )

        courseJpaRepository.save(newEntity)
    }

    override fun createCourseRegistration(
        userAccountId: Long,
        courseId: Long,
        createdAt: Instant,
    ): Either<FailureType, Unit> {
        courseJpaRepository.findById(courseId)
            ?: return FailureType.COURSE_NOT_FOUND.left()

        val newEntity = CourseRegistrationEntity(
            id = null,
            userAccountId = userAccountId,
            courseId = courseId,
            createdAt = LocalDateTime.ofInstant(createdAt, WeolbuDataSource.ZONE_OFFSET),
        )

        registrationJpaRepository.save(newEntity)
        return Unit.right()
    }

    private fun CourseAndRegistrationEntity.toDomainEntity() = Course(
        id = this.courseEntity.id!!,
        title = this.courseEntity.title,
        maxParticipants = this.courseEntity.maxParticipants,
        price = this.courseEntity.price,
        createdAt = this.courseEntity.createdAt.toInstant(WeolbuDataSource.ZONE_OFFSET),
        currentParticipants = this.currentParticipants,
        registrationRate = this.registrationRate,
    )
}
