package com.weolbu.test.infra.database.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAccountJpaRepository : JpaRepository<UserAccountEntity, Long> {
    fun findByEmail(email: String): UserAccountEntity?
}
