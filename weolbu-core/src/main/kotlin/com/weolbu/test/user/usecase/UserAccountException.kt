package com.weolbu.test.user.usecase

import com.weolbu.test.support.exception.WeolbuCommonErrorCode
import com.weolbu.test.support.exception.WeolbuException
import com.weolbu.test.support.exception.WeolbuException.ErrorCode.Type

sealed class UserAccountException(cause: Throwable? = null) : WeolbuException(cause) {
    abstract override val errorCode: ErrorCode

    class AlreadyRegisteredEmail(email: String) : UserAccountException() {
        override val errorCode = ErrorCode(
            code = "USR1001",
            displayMessage = "이미 등록된 이메일이에요.",
            type = Type.BusinessException,
        )
        override val details = "The email is already registered. email=$email"
    }

    class InvalidPasswordLength(input: String) : UserAccountException() {
        override val errorCode = ErrorCode(
            code = "USR1002",
            displayMessage = "비밀번호 길이는 최소 6자 이 10자 이하 이어야 해요.",
            type = Type.BusinessException,
        )
        override val details = "password should be between 6 and 10 characters. input=$input"
    }

    class InvalidPasswordComposition(input: String) : UserAccountException() {
        override val errorCode = ErrorCode(
            code = "USR1003",
            displayMessage = "비밀번호는 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상 조합되어야 해요.",
            type = Type.BusinessException,
        )
        override val details =
            "password should contain at least two of the following: lowercase, uppercase, numbers. input=$input"
    }

    class ExternalServiceUnavailable(override val details: String, cause: Throwable) : UserAccountException(cause) {
        override val errorCode = WeolbuCommonErrorCode.InternalServerError
    }
}
