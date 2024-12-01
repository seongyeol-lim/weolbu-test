package com.weolbu.test.course.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.weolbu.test.course.domain.CourseRepository
import com.weolbu.test.course.domain.CourseRepository.Failure.Type
import com.weolbu.test.user.domain.UserAccount
import com.weolbu.test.user.domain.UserAccountRepository
import java.time.Clock
import java.time.Instant

/**
 * 수강 신청 UseCase
 */
class RegisterCourseUseCase(
    private val clock: Clock,
    private val userAccountResponse: UserAccountRepository,
    private val courseRepository: CourseRepository,
) {
    data class Request(
        val userAccountId: Long,
        val courseIds: List<Long>,
    )

    data class Response(
        val results: List<Details>,
    ) {
        data class Details(
            val isSuccessful: Boolean,
            val courseId: Long,
            val displayMessage: String,
        )
    }

    fun register(request: Request): Either<CourseException, Response> = either {
        val userAccount: UserAccount = findUserAccountById(request.userAccountId).bind()
            ?: return CourseException.InvalidUser(request.userAccountId).left()

        val createdAt: Instant = now()
        val results: List<Response.Details> = request.courseIds
            .map { courseId: Long ->
                registerCourse(userAccount.id, courseId, createdAt)
                    .fold(
                        ifLeft = { it: CourseException ->
                            Response.Details(
                                isSuccessful = false,
                                courseId = courseId,
                                displayMessage = "강의등록에 실패했어요. - ${it.errorCode.displayMessage}",
                            )
                        },
                        ifRight = {
                            Response.Details(
                                isSuccessful = true,
                                courseId = courseId,
                                displayMessage = "[courseId=$courseId] 강의등록에 성공했어요.",
                            )
                        },
                    )
            }

        return Response(results).right()
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

    private fun registerCourse(
        userAccountId: Long,
        courseId: Long,
        createdAt: Instant,
    ): Either<CourseException, Unit> {
        val result: Either<CourseRepository.Failure, Unit> = try {
            courseRepository.createCourseRegistration(userAccountId, courseId, createdAt)
        } catch (e: Exception) {
            return CourseException.ExternalServiceUnavailable(
                details = "failed to register course. userAccountId=$userAccountId, courseId=$courseId",
                cause = e,
            ).left()
        }

        return result.mapLeft {
            when (it.type) {
                Type.COURSE_NOT_FOUND -> CourseException.CourseNotFound(userAccountId, courseId)
                Type.MAXIMUM_CAPACITY_REACHED -> CourseException.MaximumCapacityReached(userAccountId, courseId, it.courseTitle ?: "")
            }
        }
    }

    private fun now(): Instant {
        return Instant.now(clock)
    }
}
