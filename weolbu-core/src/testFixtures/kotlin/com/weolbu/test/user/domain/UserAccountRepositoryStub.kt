package com.weolbu.test.user.domain

import java.util.concurrent.ConcurrentHashMap

class UserAccountRepositoryStub(
    initialData: List<UserAccount>? = null,
) : UserAccountRepository {
    private val repository: ConcurrentHashMap<Long, UserAccount> = if (initialData == null) {
        ConcurrentHashMap()
    } else {
        ConcurrentHashMap<Long, UserAccount>().apply {
            initialData.forEach { userAccount -> this[userAccount.id] = userAccount }
        }
    }

    override fun findByEmail(email: String): UserAccount? {
        return repository.values.firstOrNull { userAccount: UserAccount -> userAccount.userInformation.email == email }
    }

    override fun saveNewUserAccount(userInformation: UserInformation, password: UserPassword): UserAccount {
        return synchronized(repository) {
            if (findByEmail(userInformation.email) != null) {
                throw RuntimeException("The email is already registered. email=${userInformation.email}")
            }

            val newUserId: Long = if (repository.isEmpty()) {
                0
            } else {
                repository.keys.max() + 1
            }

            val newUserAccount = UserAccount(
                id = newUserId,
                userInformation = userInformation,
                password = password,
            )

            repository[newUserAccount.id] = newUserAccount

            newUserAccount
        }
    }

    fun size(): Int = repository.size

    fun single(): UserAccount = repository.values.single()
}
