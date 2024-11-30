package com.weolbu.test.course.domain

import java.time.Instant

interface CourseRepository {
    fun saveNewCourse(title: String, maxParticipants: Int, price: Int, createdAt: Instant): Course
}
