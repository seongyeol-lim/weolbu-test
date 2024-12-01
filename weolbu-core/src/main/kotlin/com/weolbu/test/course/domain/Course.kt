package com.weolbu.test.course.domain

import java.time.Instant

/** 강의 정보 */
data class Course(
    /** 강의 ID */
    val id: Long,

    /** 강의명 */
    val title: String,

    /** 최대 수강 인원 */
    val maxParticipants: Int,

    /** 가격 */
    val price: Int,

    /** 강의 등록일 */
    val createdAt: Instant,

    /** 현재 수강 인원 */
    val currentParticipants: Int,
)
