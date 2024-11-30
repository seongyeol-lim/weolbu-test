package com.weolbu.test.user.domain

interface UserAccountRepository {
    fun findByEmail(email: String): UserAccount?

    fun saveNewUserAccount(userInformation: UserInformation, password: UserPassword): UserAccount
}
