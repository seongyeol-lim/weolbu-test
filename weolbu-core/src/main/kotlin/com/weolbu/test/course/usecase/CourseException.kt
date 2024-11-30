package com.weolbu.test.course.usecase

import com.weolbu.test.support.exception.WeolbuCommonErrorCode
import com.weolbu.test.support.exception.WeolbuException
import com.weolbu.test.support.exception.WeolbuException.ErrorCode.Type

sealed class CourseException(cause: Throwable? = null) : WeolbuException(cause) {
    abstract override val errorCode: ErrorCode

    class InvalidUser(userAccountId: Long) : CourseException() {
        override val errorCode = ErrorCode(
            code = "COR1001",
            displayMessage = "로그인을 다시 해주세요.",
            type = Type.BusinessException,
        )
        override val details = "User not found. userAccountId=$userAccountId"
    }

    class NotInstructorException(userAccountId: Long) : CourseException() {
        override val errorCode = ErrorCode(
            code = "COR1002",
            displayMessage = "강사 권한이 필요한 기능이에요.",
            type = Type.BusinessException,
        )
        override val details = "Course creation failed. User is not an instructor. userAccountId=$userAccountId"
    }

    class ExternalServiceUnavailable(override val details: String, cause: Throwable) : CourseException(cause) {
        override val errorCode = WeolbuCommonErrorCode.InternalServerError
    }
}
