package com.weolbu.test.course.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.weolbu.test.course.domain.Course
import com.weolbu.test.course.domain.CourseRepository
import com.weolbu.test.user.domain.UserAccount
import com.weolbu.test.user.domain.UserAccountRepository
import com.weolbu.test.user.domain.UserType
import java.time.Clock
import java.time.Instant

/**
 * 강의 등록 UseCase
 */
class CreateCourseUseCase(
    private val clock: Clock,
    private val userAccountResponse: UserAccountRepository,
    private val courseRepository: CourseRepository,
) {
    data class Request(
        val userAccountId: Long,
        val title: String,
        val maxParticipants: Int,
        val price: Int,
    )

    data object Response

    fun create(request: Request): Either<CourseException, Response> = either {
        checkIfInstructor(request.userAccountId).bind()

        val createdAt: Instant = now()
        saveNewCourse(request, createdAt).bind()

        return Response.right()
    }

    private fun checkIfInstructor(userAccountId: Long): Either<CourseException, Unit> = either {
        val userAccount: UserAccount = findUserAccountById(userAccountId).bind()
            ?: return CourseException.InvalidUser(userAccountId).left()

        return if (userAccount.userInformation.userType != UserType.INSTRUCTOR) {
            CourseException.NotInstructorException(userAccountId).left()
        } else {
            Unit.right()
        }
    }

    private fun findUserAccountById(userAccountId: Long): Either<CourseException, UserAccount?> {
        return try {
            userAccountResponse.findById(userAccountId).right()
        } catch (e: Exception) {
            CourseException.ExternalServiceUnavailable(
                details = "failed to retrieve user-account from repository",
                cause = e,
            ).left()
        }
    }

    private fun saveNewCourse(request: Request, createdAt: Instant): Either<CourseException, Course> {
        return try {
            courseRepository.saveNewCourse(
                title = request.title,
                maxParticipants = request.maxParticipants,
                price = request.price,
                createdAt = createdAt,
            ).right()
        } catch (e: Exception) {
            CourseException.ExternalServiceUnavailable(
                details = "failed to save new course to repository",
                cause = e,
            ).left()
        }
    }

    private fun now(): Instant {
        return Instant.now(clock)
    }
}
