package com.weolbu.test.course.domain

import java.time.Instant

/** 수강 신청 */
data class CourseRegistration(
    /** 수강생 */
    val userAccountId: Long,

    /** 강의 ID */
    val courseId: Long,

    /** 신청일 */
    val createdAt: Instant,
)
