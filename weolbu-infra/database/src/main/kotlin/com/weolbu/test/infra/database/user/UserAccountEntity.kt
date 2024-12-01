package com.weolbu.test.infra.database.user

import com.weolbu.test.user.domain.UserType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user_account")
class UserAccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long?,

    @Column(name = "name", nullable = false, updatable = false)
    val name: String,

    @Column(name = "email", nullable = false, updatable = false)
    val email: String,

    @Column(name = "phone_number", nullable = false, updatable = false)
    val phoneNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(
        name = "user_type",
        nullable = false,
        updatable = false,
        columnDefinition = "ENUM ('INSTRUCTOR', 'STUDENT')",
    )
    val userType: UserType,

    @Column(name = "password_digest", nullable = false, updatable = false)
    val passwordDigest: String,
)
