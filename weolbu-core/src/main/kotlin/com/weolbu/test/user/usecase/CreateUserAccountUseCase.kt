package com.weolbu.test.user.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.weolbu.test.user.domain.PasswordPolicy
import com.weolbu.test.user.domain.UserAccount
import com.weolbu.test.user.domain.UserAccountRepository
import com.weolbu.test.user.domain.UserInformation
import com.weolbu.test.user.domain.UserPassword

/**
 * 회원 가입 UseCase
 */
class CreateUserAccountUseCase(
    private val repository: UserAccountRepository,
) {
    data class Request(
        val userInformation: UserInformation,
        val password: String,
    )

    data object Response

    fun create(request: Request): Either<UserAccountException, Response> = either {
        checkEmailAlreadyRegistered(request.userInformation.email).bind()

        val password: UserPassword = createPassword(request.password).bind()
        saveUserAccount(request.userInformation, password).bind()

        return Response.right()
    }

    private fun checkEmailAlreadyRegistered(email: String): Either<UserAccountException, Unit> {
        return try {
            if (repository.findByEmail(email) != null) {
                UserAccountException.AlreadyRegisteredEmail(email).left()
            } else {
                Unit.right()
            }
        } catch (e: Exception) {
            UserAccountException.ExternalServiceUnavailable(
                details = "failed to retrieve user-account from repository",
                cause = e,
            ).left()
        }
    }

    private fun createPassword(password: String): Either<UserAccountException, UserPassword> {
        return UserPassword.create(password)
            .mapLeft {
                when (it) {
                    PasswordPolicy.PASSWORD_LENGTH -> UserAccountException.InvalidPasswordLength(password)
                    PasswordPolicy.CHARACTER_COMBINATION -> UserAccountException.InvalidPasswordComposition(password)
                }
            }
    }

    private fun saveUserAccount(
        userInformation: UserInformation,
        password: UserPassword,
    ): Either<UserAccountException, UserAccount> {
        return try {
            repository.saveNewUserAccount(userInformation, password).right()
        } catch (e: Exception) {
            UserAccountException.ExternalServiceUnavailable(
                details = "failed to save new user-account to repository",
                cause = e,
            ).left()
        }
    }
}
