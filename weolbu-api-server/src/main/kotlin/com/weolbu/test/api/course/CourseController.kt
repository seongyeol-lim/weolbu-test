package com.weolbu.test.api.course

import arrow.core.getOrElse
import com.weolbu.test.course.domain.Course
import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.course.usecase.CreateCourseUseCase
import com.weolbu.test.course.usecase.ListCourseUseCase
import com.weolbu.test.course.usecase.RegisterCourseUseCase
import com.weolbu.test.support.data.OffsetPageRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "course", description = "강의 API")
@RestController
class CourseController(
    private val createCourseUseCase: CreateCourseUseCase,
    private val listCourseUseCase: ListCourseUseCase,
    private val registerCourseUseCase: RegisterCourseUseCase,
) {
    @Schema(name = "강의 개설 API RequestBody")
    data class CreateCourseRequest(
        val userAccountId: Long,
        val title: String,
        val maxParticipants: Long,
        val price: Long,
    )

    @PostMapping("/courses")
    @Operation(summary = "강의 개설 API", description = "새로운 강의를 개설해요.")
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

    @Schema(name = "강의 조회 API ResponseBody")
    data class ListCoursesResponse(
        val pageNum: Int,
        val pageSize: Int,
        val totalElements: Int,
        val items: List<Course>,
    )

    @GetMapping("/courses")
    @Operation(summary = "강의 조회 API", description = "강의 리스트를 조회해요.")
    fun listCourses(
        @RequestParam(name = "page") page: Int?,
        @RequestParam(name = "size") size: Int?,
        @RequestParam(name = "sort") sort: CourseSort?,
    ): ResponseEntity<ListCoursesResponse> {
        val pageRequest: OffsetPageRequest = OffsetPageRequest.of(pageNum = page ?: 1, pageSize = size ?: 20)
            .getOrElse { OffsetPageRequest.DEFAULT }

        return listCourseUseCase.listCourses(
            ListCourseUseCase.Request(
                pageRequest = pageRequest,
                sort = sort ?: CourseSort.RECENTLY_REGISTERED,
            ),
        )
            .map {
                ResponseEntity.ok(
                    ListCoursesResponse(
                        pageNum = it.content.pageNum,
                        pageSize = it.content.pageSize,
                        totalElements = it.content.totalElements,
                        items = it.content.items,
                    ),
                )
            }
            .getOrElse { throw it }
    }

    @Schema(name = "수강 신청 API RequestBody")
    data class RegisterCourseRequest(
        val userAccountId: Long,
        val courseIds: List<Long>,
    )

    @Schema(name = "수강 신청 API ResponseBody")
    data class RegisterCourseResponse(
        val results: List<String>,
    )

    @PostMapping("/courses/registration")
    @Operation(summary = "수강 신청 API", description = "강의 수강신청을 해요.")
    fun registerCourses(@RequestBody request: RegisterCourseRequest): ResponseEntity<RegisterCourseResponse> {
        return registerCourseUseCase.register(
            RegisterCourseUseCase.Request(userAccountId = request.userAccountId, courseIds = request.courseIds),
        ).map { response ->
            ResponseEntity.ok(RegisterCourseResponse(response.results.map { it.displayMessage }))
        }.getOrElse { throw it }
    }
}
