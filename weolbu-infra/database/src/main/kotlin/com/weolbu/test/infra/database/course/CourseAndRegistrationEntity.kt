package com.weolbu.test.infra.database.course

import com.querydsl.core.annotations.QueryProjection

class CourseAndRegistrationEntity @QueryProjection constructor(
    val courseEntity: CourseEntity,
    val currentParticipants: Long,
    val registrationRate: Double,
)
