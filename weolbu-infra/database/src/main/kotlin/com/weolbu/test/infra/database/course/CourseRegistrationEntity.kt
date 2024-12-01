package com.weolbu.test.infra.database.course

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "course_registration")
class CourseRegistrationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long?,

    @Column(name = "user_account_id", nullable = false, updatable = false)
    val userAccountId: Long,

    @Column(name = "course_id", nullable = false, updatable = false)
    val courseId: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,
)
