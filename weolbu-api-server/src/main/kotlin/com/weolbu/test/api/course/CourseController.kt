package com.weolbu.test.api.course

import arrow.core.getOrElse
import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.course.domain.CourseWithStatus
import com.weolbu.test.course.usecase.CreateCourseUseCase
import com.weolbu.test.course.usecase.ListCourseUseCase
import com.weolbu.test.course.usecase.RegisterCourseUseCase
import com.weolbu.test.support.data.OffsetPageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CourseController(
    private val createCourseUseCase: CreateCourseUseCase,
    private val listCourseUseCase: ListCourseUseCase,
    private val registerCourseUseCase: RegisterCourseUseCase,
) {
    data class CreateCourseRequest(
        val userAccountId: Long,
        val title: String,
        val maxParticipants: Long,
        val price: Long,
    )

    @PostMapping("/courses")
    fun createCourse(@RequestBody request: CreateCourseRequest): ResponseEntity<Unit> {
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

    data class ListCoursesResponse(
        val pageNum: Int,
        val pageSize: Int,
        val totalElements: Int,
        val items: List<CourseWithStatus>,
    )

    @GetMapping("/courses")
    fun listCourses(
        @RequestParam(name = "page") page: Int?,
        @RequestParam(name = "size") size: Int?,
        @RequestParam(name = "sort") sort: CourseSort?,
    ): ResponseEntity<ListCoursesResponse> {
        val pageRequest: OffsetPageRequest = OffsetPageRequest.of(pageNum = page ?: 1, pageSize = size ?: 20)
            .getOrElse { OffsetPageRequest.DEFAULT }

        return listCourseUseCase.listCourses(
            ListCourseUseCase.Request(pageRequest = pageRequest, sort = sort ?: CourseSort.RECENTLY_REGISTERED),
        ).map {
            ResponseEntity.ok(
                ListCoursesResponse(
                    pageNum = it.content.pageNum,
                    pageSize = it.content.pageSize,
                    totalElements = it.content.totalElements,
                    items = it.content.items,
                ),
            )
        }.getOrElse { throw it }
    }

    data class RegisterCourseRequest(
        val userAccountId: Long,
        val courseIds: List<Long>,
    )

    data class RegisterCourseResponse(
        val results: List<String>,
    )

    @PostMapping("/courses/registration")
    fun registerCourses(@RequestBody request: RegisterCourseRequest): ResponseEntity<RegisterCourseResponse> {
        return registerCourseUseCase.register(
            RegisterCourseUseCase.Request(userAccountId = request.userAccountId, courseIds = request.courseIds),
        ).map { response ->
            ResponseEntity.ok(RegisterCourseResponse(response.results.map { it.displayMessage }))
        }.getOrElse { throw it }
    }
}
