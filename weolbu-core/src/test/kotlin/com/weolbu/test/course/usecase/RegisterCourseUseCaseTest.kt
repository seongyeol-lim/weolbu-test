package com.weolbu.test.course.usecase

import arrow.core.Either
import com.weolbu.test.course.domain.Course
import com.weolbu.test.course.domain.CourseRepositoryStub
import com.weolbu.test.course.domain.course
import com.weolbu.test.user.domain.UserAccount
import com.weolbu.test.user.domain.UserAccountRepositoryStub
import com.weolbu.test.user.domain.UserType
import com.weolbu.test.user.domain.userAccount
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.take
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class RegisterCourseUseCaseTest : FunSpec({
    val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    context("수강생은 여러 개의 강의를 한 번에 수강신청 할 수 있어요.") {
        test("수강생은 3개의 강의를 신청하여 모두 성공해요.") {
            val studentUser: UserAccount = Arb.userAccount()
                .filter { it.userInformation.userType == UserType.STUDENT }
                .single()
            val userAccountRepository = UserAccountRepositoryStub(initialData = listOf(studentUser))

            val courses: List<Course> = Arb.course().take(3).toList()
            val courseRepository = CourseRepositoryStub(initialData = courses)

            val sut = RegisterCourseUseCase(fixedClock, userAccountRepository, courseRepository)

            val givenRequest = RegisterCourseUseCase.Request(
                userAccountId = studentUser.id,
                courseIds = courses.map { it.id },
            )

            val actual: Either<CourseException, RegisterCourseUseCase.Response> = sut.register(givenRequest)

            withClue("요청한 강의 3개 모두 성공 메시지를 응답해요") {
                actual.getOrNull().shouldNotBeNull {
                    results.size shouldBe 3
                    results.all { it.isSuccessful } shouldBe true
                    results.all { it.displayMessage.contains("성공") } shouldBe true
                    results.map { it.courseId } shouldContainAll givenRequest.courseIds
                }
            }

            withClue("해당 강의의 수강생 수가 1씩 증가해요") {
                givenRequest.courseIds.forEach { courseId: Long ->
                    courseRepository.findById(courseId)?.currentParticipants shouldBe 1
                }
            }
        }

        test("수강생은 2개의 강의를 신청하여 1개는 성공, 1개는 수강인원 초과로 실패해요.") {
            val studentUser: UserAccount = Arb.userAccount()
                .filter { it.userInformation.userType == UserType.STUDENT }
                .single()
            val userAccountRepository = UserAccountRepositoryStub(initialData = listOf(studentUser))

            val availableCourses: Course = Arb.course().single()
            val closedCourses: Course = Arb.course().single()
                .let { it.copy(currentParticipants = it.maxParticipants) }
            val courses: List<Course> = listOf(availableCourses, closedCourses)

            val courseRepository = CourseRepositoryStub(initialData = courses)

            val sut = RegisterCourseUseCase(fixedClock, userAccountRepository, courseRepository)

            val givenRequest = RegisterCourseUseCase.Request(
                userAccountId = studentUser.id,
                courseIds = courses.map { it.id },
            )

            val actual: Either<CourseException, RegisterCourseUseCase.Response> = sut.register(givenRequest)

            withClue("요청한 강의 2개 하나는 성공, 하나는 실패해요") {
                actual.getOrNull().shouldNotBeNull {
                    results.size shouldBe 2
                    results.first { it.courseId == availableCourses.id }.should {
                        it.isSuccessful shouldBe true
                        it.displayMessage.contains("성공")
                    }
                    results.first { it.courseId == closedCourses.id }.should {
                        it.isSuccessful shouldBe false
                        it.displayMessage.contains("실패")
                    }
                }
            }

            withClue("수강 신청이 가능했던 강의는 수강생 수가 1 증가해요") {
                courseRepository.findById(availableCourses.id)?.currentParticipants shouldBe 1
            }

            withClue("수강 신청이 불가능했던 강의의 수강생 수는 변함이 없어요") {
                val closedCourse = courseRepository.findById(closedCourses.id)!!
                closedCourse.currentParticipants shouldBe closedCourse.maxParticipants
            }
        }
    }
})
