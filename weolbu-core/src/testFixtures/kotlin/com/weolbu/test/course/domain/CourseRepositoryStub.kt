package com.weolbu.test.course.domain

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

    override fun saveNewCourse(title: String, maxParticipants: Int, price: Int, createdAt: Instant): Course {
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
            )

            repository[newCourse.id] = newCourse

            newCourse
        }
    }

    fun size(): Int = repository.size

    fun single(): Course = repository.values.single()
}
