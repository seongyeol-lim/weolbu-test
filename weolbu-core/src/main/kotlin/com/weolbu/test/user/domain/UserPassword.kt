package com.weolbu.test.user.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.security.MessageDigest
import java.util.HexFormat

data class UserPassword(val passwordDigest: String) {
    fun matches(plainText: String): Boolean {
        return plainText.toPasswordDigest() == this.passwordDigest
    }

    companion object {
        sealed class Result {
            data class Success(val value: UserPassword) : Result()
            data class Failure(val policyViolated: PasswordPolicy) : Result()
        }

        fun create(passwordText: String): Either<PasswordPolicy, UserPassword> {
            val violationResult: PasswordPolicy? = validatePasswordPolicy(passwordText)
            if (violationResult != null) {
                return violationResult.left()
            }

            return UserPassword(passwordText.toPasswordDigest()).right()
        }

        private fun validatePasswordPolicy(passwordText: String): PasswordPolicy? {
            return PasswordPolicy.entries.firstOrNull { !it.validator.isValid(passwordText) }
        }

        private fun String.toPasswordDigest(): String {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val bytes = messageDigest.digest(this.toByteArray())

            return HexFormat.of().formatHex(bytes)
        }
    }
}

enum class PasswordPolicy(internal val validator: Validator) {
    /** 최소 6자 이상 10자 이하 */
    PASSWORD_LENGTH(validator = passwordLengthValidator),

    /** 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상 조합 필요 */
    CHARACTER_COMBINATION(validator = passwordCompositionValidator),
    ;

    internal fun interface Validator {
        fun isValid(password: String): Boolean
    }
}

private val passwordLengthValidator = PasswordPolicy.Validator { password: String ->
    password.length in 6..10
}

private val passwordCompositionValidator = PasswordPolicy.Validator { password: String ->
    val hasLowercase: Boolean = password.any { char -> char.isLowerCase() }
    val hasUppercase: Boolean = password.any { char -> char.isUpperCase() }
    val hasDigit: Boolean = password.any { char -> char.isDigit() }

    val count = listOf(hasLowercase, hasUppercase, hasDigit).filter { it }.size
    count >= 2
}
