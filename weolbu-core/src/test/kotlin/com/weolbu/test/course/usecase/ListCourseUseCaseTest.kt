package com.weolbu.test.course.usecase

import arrow.core.Either
import com.weolbu.test.course.domain.CourseRepositoryStub
import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.course.domain.CourseWithStatus
import com.weolbu.test.course.domain.course
import com.weolbu.test.course.domain.sequence
import com.weolbu.test.course.domain.withCourseStatus
import com.weolbu.test.support.data.OffsetPageRequest
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSortedDescendingBy
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.take

class ListCourseUseCaseTest : FunSpec({
    val numOfCourses = 100
    val courses: List<CourseWithStatus> = Arb
        .course(arbId = Arb.sequence(start = 0))
        .map { it.withCourseStatus(currentParticipants = 0) }
        .take(numOfCourses)
        .toList()

    val courseRepository = CourseRepositoryStub(initialData = courses)

    context("(강의가 $numOfCourses 개 등록된 상태에서) 현재 강의 정보 리스트를 조회할 수 있어요.") {
        test("강의 정보를 <최근 등록순> 으로 1번 페이지를 조회해요") {
            val sut = ListCourseUseCase(courseRepository)

            val givenRequest = ListCourseUseCase.Request(
                pageRequest = OffsetPageRequest.of(pageNum = 1, pageSize = 20).getOrThrow(),
                sort = CourseSort.RECENTLY_REGISTERED,
            )

            val actual: Either<CourseException, ListCourseUseCase.Response> = sut.listCourses(givenRequest)

            withClue("pageSize(${givenRequest.pageRequest.pageSize}) 만큼 강의를 최근 등록순으로 정렬하여 응답") {
                actual.getOrNull()?.content.shouldNotBeNull {
                    this.pageSize shouldBe givenRequest.pageRequest.pageSize
                    this.pageNum shouldBe givenRequest.pageRequest.pageNum
                    this.totalElements shouldBe courseRepository.size()
                    this.items.size shouldBe givenRequest.pageRequest.pageSize
                    this.items.shouldBeSortedDescendingBy { it.course.createdAt }

                    println("<최근 등록순>${this.items.joinToString("\n- ")}")
                }
            }
        }

        test("강의 정보를 <신청자 많은 순> 으로 1번 페이지를 조회해요") {
            val sut = ListCourseUseCase(courseRepository)

            val givenRequest = ListCourseUseCase.Request(
                pageRequest = OffsetPageRequest.of(pageNum = 1, pageSize = 20).getOrThrow(),
                sort = CourseSort.MOST_APPLICANTS,
            )

            val actual: Either<CourseException, ListCourseUseCase.Response> = sut.listCourses(givenRequest)

            withClue("pageSize(${givenRequest.pageRequest.pageSize}) 만큼 강의를 신청자 많은 순으로 정렬하여 응답") {
                actual.getOrNull()?.content.shouldNotBeNull {
                    this.pageSize shouldBe givenRequest.pageRequest.pageSize
                    this.pageNum shouldBe givenRequest.pageRequest.pageNum
                    this.totalElements shouldBe courseRepository.size()
                    this.items.size shouldBe givenRequest.pageRequest.pageSize
                    this.items.shouldBeSortedDescendingBy { course -> course.currentParticipants }

                    println("<신청자 많은 순 조회>${this.items.joinToString("\n- ")}")
                }
            }
        }

        test("강의 정보를 <신청률 높은 순> 으로 1번 페이지를 조회해요") {
            val sut = ListCourseUseCase(courseRepository)

            val givenRequest = ListCourseUseCase.Request(
                pageRequest = OffsetPageRequest.of(pageNum = 1, pageSize = 20).getOrThrow(),
                sort = CourseSort.HIGHEST_APPLICATION_RATE,
            )

            val actual: Either<CourseException, ListCourseUseCase.Response> = sut.listCourses(givenRequest)

            withClue("pageSize(${givenRequest.pageRequest.pageSize}) 만큼 강의를 신청률 높은 순으로 정렬하여 응답") {
                actual.getOrNull()?.content.shouldNotBeNull {
                    this.pageSize shouldBe givenRequest.pageRequest.pageSize
                    this.pageNum shouldBe givenRequest.pageRequest.pageNum
                    this.totalElements shouldBe courseRepository.size()
                    this.items.size shouldBe givenRequest.pageRequest.pageSize
                    this.items.shouldBeSortedDescendingBy { it.currentParticipants.toDouble() / it.course.maxParticipants }

                    println("<신청률 높은 순 조회>${this.items.joinToString("\n- ")}")
                }
            }
        }

        test("페이지 범위를 넘어서는 값을 조회하면 -> empty list 를 응답해요") {
            val sut = ListCourseUseCase(courseRepository)

            val givenRequest = ListCourseUseCase.Request(
                pageRequest = OffsetPageRequest.of(pageNum = 999, pageSize = 20).getOrThrow(),
                sort = CourseSort.RECENTLY_REGISTERED,
            )

            val actual: Either<CourseException, ListCourseUseCase.Response> = sut.listCourses(givenRequest)

            withClue("empty list 응답") {
                actual.getOrNull()?.content.shouldNotBeNull {
                    this.pageSize shouldBe givenRequest.pageRequest.pageSize
                    this.pageNum shouldBe givenRequest.pageRequest.pageNum
                    this.totalElements shouldBe courseRepository.size()
                    this.items.size shouldBe 0
                }
            }
        }
    }
})
