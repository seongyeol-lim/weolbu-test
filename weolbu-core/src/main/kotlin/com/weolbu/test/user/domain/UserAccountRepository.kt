package com.weolbu.test.user.domain

interface UserAccountRepository {
    fun findById(id: Long): UserAccount?

    fun findByEmail(email: String): UserAccount?

    fun saveNewUserAccount(userInformation: UserInformation, password: UserPassword): UserAccount
}
