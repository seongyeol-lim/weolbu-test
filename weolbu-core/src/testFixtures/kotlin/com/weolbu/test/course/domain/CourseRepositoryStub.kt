package com.weolbu.test.course.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.weolbu.test.course.domain.CourseRepository.FailureType
import com.weolbu.test.course.domain.CourseSort.HIGHEST_APPLICATION_RATE
import com.weolbu.test.course.domain.CourseSort.MOST_APPLICANTS
import com.weolbu.test.course.domain.CourseSort.RECENTLY_REGISTERED
import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class CourseRepositoryStub(
    initialData: List<Course>? = null,
) : CourseRepository {
    private val repository: ConcurrentHashMap<Long, Course> = if (initialData == null) {
        ConcurrentHashMap()
    } else {
        ConcurrentHashMap<Long, Course>().apply {
            initialData.forEach { course -> this[course.id] = course }
        }
    }

    override fun getAllCourse(pageRequest: OffsetPageRequest, sort: CourseSort): OffsetPageContent<Course> {
        val courses: List<Course> = when (sort) {
            RECENTLY_REGISTERED -> repository.values.sortedByDescending { it.createdAt }
            MOST_APPLICANTS -> repository.values.sortedByDescending { it.currentParticipants }
            HIGHEST_APPLICATION_RATE -> repository.values.sortedByDescending { it.currentParticipants.toDouble() / it.maxParticipants }
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

            val newCourse = Course(
                id = newCourseId,
                title = title,
                maxParticipants = maxParticipants,
                price = price,
                createdAt = createdAt,
                currentParticipants = 0,
                registrationRate = 0.toDouble(),
            )

            repository[newCourse.id] = newCourse
        }
    }

    override fun createCourseRegistration(
        userAccountId: Long,
        courseId: Long,
        createdAt: Instant,
    ): Either<FailureType, CourseRegistration> {
        synchronized(repository) {
            val course: Course = repository[courseId]
                ?: return FailureType.COURSE_NOT_FOUND.left()

            if (course.currentParticipants == course.maxParticipants) {
                return FailureType.MAXIMUM_CAPACITY_REACHED.left()
            }

            repository[courseId] = course.copy(currentParticipants = course.currentParticipants + 1)
        }

        return CourseRegistration(userAccountId = userAccountId, courseId = courseId, createdAt = createdAt).right()
    }

    fun findById(id: Long): Course? {
        return repository[id]
    }

    fun size(): Int = repository.size

    fun single(): Course = repository.values.single()
}
