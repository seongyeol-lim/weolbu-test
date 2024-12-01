package com.weolbu.test.course.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.weolbu.test.course.domain.Course
import com.weolbu.test.course.domain.CourseRepository
import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest

/**
 * 강의 정보 조회 UseCase
 */
class ListCourseUseCase(
    private val repository: CourseRepository,
) {
    data class Request(
        val pageRequest: OffsetPageRequest,
        val sort: CourseSort,
    )

    data class Response(
        val content: OffsetPageContent<Course>,
    )

    fun listCourses(request: Request): Either<CourseException, Response> = either {
        val courses: OffsetPageContent<Course> = try {
            repository.getAllCourse(
                pageRequest = OffsetPageRequest.of(
                    pageSize = request.pageRequest.pageSize,
                    pageNum = request.pageRequest.pageNum,
                ).getOrDefault(OffsetPageRequest.DEFAULT),
                sort = request.sort,
            )
        } catch (e: Exception) {
            return CourseException.ExternalServiceUnavailable(
                details = "failed to retrieve course from repository",
                cause = e,
            ).left()
        }

        return Response(courses).right()
    }
}
