package com.weolbu.test.course.domain

import arrow.core.Either
import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest
import java.time.Instant

interface CourseRepository {
    fun getAllCourse(pageRequest: OffsetPageRequest, sort: CourseSort): OffsetPageContent<Course>

    fun saveNewCourse(title: String, maxParticipants: Long, price: Long, createdAt: Instant)

    fun createCourseRegistration(
        userAccountId: Long,
        courseId: Long,
        createdAt: Instant,
    ): Either<Failure, Unit>

    data class Failure(
        val type: Type,
        val courseTitle: String?,
    ) {
        enum class Type {
            /** 요청한 강의가 존재하지 않는 경우 */
            COURSE_NOT_FOUND,

            /** 최대 수강 인원 도달 */
            MAXIMUM_CAPACITY_REACHED,
        }
    }
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
