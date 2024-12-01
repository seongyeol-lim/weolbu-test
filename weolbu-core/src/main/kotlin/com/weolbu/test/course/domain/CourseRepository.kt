package com.weolbu.test.course.domain

import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest
import java.time.Instant

interface CourseRepository {
    fun getAllCourse(pageRequest: OffsetPageRequest, sort: CourseSort): OffsetPageContent<Course>

    fun saveNewCourse(title: String, maxParticipants: Int, price: Int, createdAt: Instant): Course
}

/** 강의 정보 정렬 방법 */
enum class CourseSort {
    /** 최근 등록 순 */
    RECENTLY_REGISTERED,

    /** 신청자 많은 순 */
    MOST_APPLICANTS,

    /** 신청률 높은 순 */
    HIGHEST_APPLICATION_RATE,
}
