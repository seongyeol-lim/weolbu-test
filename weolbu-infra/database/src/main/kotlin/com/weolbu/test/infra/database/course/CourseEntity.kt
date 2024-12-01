package com.weolbu.test.infra.database.course

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "course")
class CourseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long?,

    @Column(name = "title", nullable = false, updatable = false)
    val title: String,

    @Column(name = "max_participants", nullable = false, updatable = false)
    val maxParticipants: Int,

    @Column(name = "price", nullable = false, updatable = false)
    val price: Int,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,
)
