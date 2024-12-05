package com.weolbu.test.course.usecase

import com.weolbu.test.support.exception.WeolbuErrorCode
import com.weolbu.test.support.exception.WeolbuErrorCode.Type
import com.weolbu.test.support.exception.WeolbuException

sealed class CourseException(cause: Throwable? = null) : WeolbuException(cause) {
    abstract override val errorCode: WeolbuErrorCode

    class InvalidUser(userAccountId: Long) : CourseException() {
        override val errorCode = WeolbuErrorCode(
            code = "COR1001",
            displayMessage = "로그인을 다시 해주세요.",
            details = "User not found. userAccountId=$userAccountId",
            type = Type.BusinessException,
        )
    }

    class NotInstructorException(userAccountId: Long) : CourseException() {
        override val errorCode = WeolbuErrorCode(
            code = "COR1002",
            displayMessage = "강사 권한이 필요한 기능이에요.",
            details = "Course creation failed. User is not an instructor. userAccountId=$userAccountId",
            type = Type.BusinessException,
        )
    }

    class CourseNotFound(userAccountId: Long, courseId: Long) : CourseException() {
        override val errorCode = WeolbuErrorCode(
            code = "COR1003",
            displayMessage = "[courseId=$courseId] 요청한 강의를 찾지 못했어요.",
            details = "Course not found. userAccountId=$userAccountId, courseId=$courseId",
            type = Type.BusinessException,
        )
    }

    class MaximumCapacityReached(userAccountId: Long, courseId: Long, courseTitle: String) : CourseException() {
        override val errorCode = WeolbuErrorCode(
            code = "COR1004",
            displayMessage = "[$courseTitle] 최대 수강 인원 도달로 인해 수강 신청에 실패했어요.",
            details = "Course not found. userAccountId=$userAccountId, courseId=$courseId",
            type = Type.BusinessException,
        )
    }

    class ExternalServiceUnavailable(details: String, cause: Throwable) : CourseException(cause) {
        override val errorCode = WeolbuErrorCode.internalError(details)
    }
}
