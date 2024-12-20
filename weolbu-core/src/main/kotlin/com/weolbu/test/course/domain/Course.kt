package com.weolbu.test.course.domain

import java.time.Instant

/** 강의 정보 */
data class Course(
    /** 강의 ID */
    val id: Long,

    /** 강의명 */
    val title: String,

    /** 최대 수강 인원 */
    val maxParticipants: Long,

    /** 가격 */
    val price: Long,

    /** 강의 등록일 */
    val createdAt: Instant,
)

/** 강의 정보 및 수강 신청 상태 정보 */
data class CourseWithStatus(
    val course: Course,

    /** 현재 수강 인원 */
    val currentParticipants: Long,

    /** 강의 신청률 */
    val registrationRate: Double,
)
