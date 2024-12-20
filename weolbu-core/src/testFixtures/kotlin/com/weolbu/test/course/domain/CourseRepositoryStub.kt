package com.weolbu.test.course.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.weolbu.test.course.domain.CourseRepository.Failure
import com.weolbu.test.course.domain.CourseSort.HIGHEST_APPLICATION_RATE
import com.weolbu.test.course.domain.CourseSort.MOST_APPLICANTS
import com.weolbu.test.course.domain.CourseSort.RECENTLY_REGISTERED
import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class CourseRepositoryStub(
    initialData: List<CourseWithStatus>? = null,
) : CourseRepository {
    private val repository: ConcurrentHashMap<Long, CourseWithStatus> = if (initialData == null) {
        ConcurrentHashMap()
    } else {
        ConcurrentHashMap<Long, CourseWithStatus>().apply {
            initialData.forEach { this[it.course.id] = it }
        }
    }

    override fun getAllCourse(pageRequest: OffsetPageRequest, sort: CourseSort): OffsetPageContent<CourseWithStatus> {
        val courses: List<CourseWithStatus> = when (sort) {
            RECENTLY_REGISTERED -> repository.values.sortedByDescending { it.course.createdAt }
            MOST_APPLICANTS -> repository.values.sortedByDescending { it.currentParticipants }
            HIGHEST_APPLICATION_RATE -> repository.values.sortedByDescending { it.currentParticipants.toDouble() / it.course.maxParticipants }
        }

        val offset: Int = pageRequest.offset()
        return OffsetPageContent(
            pageSize = pageRequest.pageSize,
            pageNum = pageRequest.pageNum,
            totalElements = courses.size,
            items = if (offset >= courses.size) {
                emptyList()
            } else {
                courses.subList(offset, kotlin.math.min(offset + pageRequest.pageSize, courses.size))
            },
        )
    }

    override fun saveNewCourse(title: String, maxParticipants: Long, price: Long, createdAt: Instant) {
        return synchronized(repository) {
            val newCourseId: Long = if (repository.isEmpty()) {
                0
            } else {
                repository.keys.max() + 1
            }

            val newCourse = CourseWithStatus(
                course = Course(
                    id = newCourseId,
                    title = title,
                    maxParticipants = maxParticipants,
                    price = price,
                    createdAt = createdAt,
                ),
                currentParticipants = 0,
                registrationRate = 0.toDouble(),
            )

            repository[newCourse.course.id] = newCourse
        }
    }

    override fun createCourseRegistration(
        userAccountId: Long,
        courseId: Long,
        createdAt: Instant,
    ): Either<Failure, Unit> {
        synchronized(repository) {
            val course: CourseWithStatus = repository[courseId]
                ?: return Failure(type = Failure.Type.COURSE_NOT_FOUND, courseTitle = null).left()

            if (course.currentParticipants == course.course.maxParticipants) {
                return Failure(type = Failure.Type.MAXIMUM_CAPACITY_REACHED, courseTitle = course.course.title).left()
            }

            repository[courseId] = course.copy(currentParticipants = course.currentParticipants + 1)
        }

        return Unit.right()
    }

    fun findById(id: Long): CourseWithStatus? {
        return repository[id]
    }

    fun size(): Int = repository.size

    fun single(): CourseWithStatus = repository.values.single()
}
