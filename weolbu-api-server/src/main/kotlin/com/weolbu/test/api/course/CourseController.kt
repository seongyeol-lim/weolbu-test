package com.weolbu.test.api.course

import arrow.core.getOrElse
import com.weolbu.test.contract.apis.CoursesApi
import com.weolbu.test.contract.models.CreateCourseRequest
import com.weolbu.test.contract.models.ListCourse200Response
import com.weolbu.test.contract.models.RegisterCourse200Response
import com.weolbu.test.contract.models.RegisterCourseRequest
import com.weolbu.test.course.domain.Course
import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.course.domain.CourseWithStatus
import com.weolbu.test.course.usecase.CreateCourseUseCase
import com.weolbu.test.course.usecase.ListCourseUseCase
import com.weolbu.test.course.usecase.RegisterCourseUseCase
import com.weolbu.test.support.data.OffsetPageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CourseController(
    private val createCourseUseCase: CreateCourseUseCase,
    private val listCourseUseCase: ListCourseUseCase,
    private val registerCourseUseCase: RegisterCourseUseCase,
) : CoursesApi {
    override fun createCourse(request: CreateCourseRequest): ResponseEntity<Unit> {
        return createCourseUseCase.create(
            CreateCourseUseCase.Request(
                userAccountId = request.userAccountId,
                title = request.title,
                maxParticipants = request.maxParticipants,
                price = request.price,
            ),
        )
            .map { ResponseEntity.noContent().build<Unit>() }
            .getOrElse { throw it }
    }

    override fun listCourse(page: Int?, size: Int?, sort: String?): ResponseEntity<ListCourse200Response> {
        val pageRequest: OffsetPageRequest = OffsetPageRequest.of(pageNum = page ?: 1, pageSize = size ?: 20)
            .getOrElse { OffsetPageRequest.DEFAULT }

        return listCourseUseCase.listCourses(
            ListCourseUseCase.Request(pageRequest = pageRequest, sort = sort?.toCourseSort() ?: CourseSort.RECENTLY_REGISTERED),
        ).map {
            ResponseEntity.ok(
                ListCourse200Response(
                    pageNum = it.content.pageNum,
                    pageSize = it.content.pageSize,
                    totalElements = it.content.totalElements,
                    items = it.content.items.map { it.toProtocolDomain() },
                ),
            )
        }.getOrElse { throw it }
    }

    override fun registerCourse(request: RegisterCourseRequest): ResponseEntity<RegisterCourse200Response> {
        return registerCourseUseCase.register(
            RegisterCourseUseCase.Request(userAccountId = request.userAccountId, courseIds = request.courseIds),
        ).map { response ->
            ResponseEntity.ok(RegisterCourse200Response(response.results.map { it.displayMessage }))
        }.getOrElse { throw it }
    }

    private fun String.toCourseSort(): CourseSort? {
        return CourseSort.entries.firstOrNull { it.name == this }
    }

    private fun CourseWithStatus.toProtocolDomain() = com.weolbu.test.contract.models.CourseWithStatus(
        course = this.course.toProtocolDomain(),
        currentParticipants = this.currentParticipants,
        registrationRate = this.registrationRate.toFloat(),
    )

    private fun Course.toProtocolDomain() = com.weolbu.test.contract.models.Course(
        id = this.id,
        title = this.title,
        maxParticipants = this.maxParticipants,
        price = this.price,
        createdAt = this.createdAt.toString(),
    )
}
