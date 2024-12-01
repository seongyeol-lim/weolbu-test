package com.weolbu.test.adapter.user

import com.weolbu.test.infra.database.user.UserAccountEntity
import com.weolbu.test.infra.database.user.UserAccountJpaRepository
import com.weolbu.test.user.domain.UserAccount
import com.weolbu.test.user.domain.UserAccountRepository
import com.weolbu.test.user.domain.UserInformation
import com.weolbu.test.user.domain.UserPassword
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class UserAccountRepositoryAdapter(
    private val jpaRepository: UserAccountJpaRepository,
) : UserAccountRepository {
    override fun findById(id: Long): UserAccount? {
        return jpaRepository.findById(id).map { it.toDomainEntity() }.getOrNull()
    }

    override fun findByEmail(email: String): UserAccount? {
        return jpaRepository.findByEmail(email)?.toDomainEntity()
    }

    override fun saveNewUserAccount(userInformation: UserInformation, password: UserPassword): UserAccount {
        val jpaEntity = UserAccountEntity(
            id = null,
            name = userInformation.name,
            email = userInformation.email,
            phoneNumber = userInformation.phoneNumber,
            userType = userInformation.userType,
            passwordDigest = password.passwordDigest,
        )

        return jpaRepository.save(jpaEntity).toDomainEntity()
    }

    private fun UserAccountEntity.toDomainEntity() = UserAccount(
        id = this.id!!,
        userInformation = UserInformation(
            name = this.name,
            email = this.email,
            phoneNumber = this.phoneNumber,
            userType = this.userType,
        ),
        password = UserPassword(passwordDigest = this.passwordDigest),
    )
}
