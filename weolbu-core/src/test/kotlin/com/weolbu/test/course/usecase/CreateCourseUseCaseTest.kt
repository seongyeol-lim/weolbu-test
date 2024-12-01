package com.weolbu.test.course.usecase

import arrow.core.Either
import com.weolbu.test.course.domain.Course
import com.weolbu.test.course.domain.CourseRepository
import com.weolbu.test.course.domain.CourseRepositoryStub
import com.weolbu.test.course.domain.courseTitle
import com.weolbu.test.user.domain.UserAccount
import com.weolbu.test.user.domain.UserAccountRepository
import com.weolbu.test.user.domain.UserAccountRepositoryStub
import com.weolbu.test.user.domain.instructorUserAccount
import com.weolbu.test.user.domain.studentUserAccount
import com.weolbu.test.user.domain.userAccountId
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.single
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CreateCourseUseCaseTest : FunSpec({
    val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    test("강사는 강의 정보를 입력하여 강의 등록을 할 수 있어요.") {
        val instructorUser: UserAccount = Arb.instructorUserAccount().single()
        val userAccountRepositoryStub = UserAccountRepositoryStub(initialData = listOf(instructorUser))

        val courseRepositoryStub = CourseRepositoryStub(initialData = null)
        val sut = CreateCourseUseCase(fixedClock, userAccountRepositoryStub, courseRepositoryStub)

        val givenRequest = CreateCourseUseCase.Request(
            userAccountId = instructorUser.id,
            title = Arb.courseTitle().single(),
            maxParticipants = Arb.long(10L..100).single(),
            price = Arb.long(1000L..50000).single(),
        )

        val actual: Either<CourseException, CreateCourseUseCase.Response> = sut.create(givenRequest)

        withClue("요청이 성공적으로 처리되며, 별도의 응답 값은 없어요") {
            actual.getOrNull() shouldBe CreateCourseUseCase.Response
        }

        withClue("Repository 에 신규 강의가 저장되어요") {
            courseRepositoryStub.single() shouldBe Course(
                id = 0,
                title = givenRequest.title,
                maxParticipants = givenRequest.maxParticipants,
                price = givenRequest.price,
                createdAt = fixedClock.instant(),
                currentParticipants = 0,
                registrationRate = 0.toDouble(),
            )
        }
    }

    test("강사가 아닌 수강생이 강의 등록을 하는 경우 -> COR1002 에러코드를 응답해요.") {
        val studentUser: UserAccount = Arb.studentUserAccount().single()
        val userAccountRepositoryStub = UserAccountRepositoryStub(initialData = listOf(studentUser))

        val courseRepositoryStub = CourseRepositoryStub(initialData = null)
        val sut = CreateCourseUseCase(fixedClock, userAccountRepositoryStub, courseRepositoryStub)

        val givenRequest = CreateCourseUseCase.Request(
            userAccountId = studentUser.id,
            title = Arb.courseTitle().single(),
            maxParticipants = Arb.long(10L..100).single(),
            price = Arb.long(1000L..50000).single(),
        )

        val actual: Either<CourseException, CreateCourseUseCase.Response> = sut.create(givenRequest)

        withClue("COR1002 에러코드 응답") {
            actual.leftOrNull()
                .shouldBeInstanceOf<CourseException.NotInstructorException>()
                .should {
                    it.errorCode.code shouldBe "COR1002"
                }
        }
    }

    test("존재하지 않는 userAccountId 로 요청한 경우 -> COR1001 에러코드를 응답해요.") {
        val userAccountRepositoryStub = UserAccountRepositoryStub(initialData = null)

        val courseRepositoryStub = CourseRepositoryStub(initialData = null)
        val sut = CreateCourseUseCase(fixedClock, userAccountRepositoryStub, courseRepositoryStub)

        val givenRequest = CreateCourseUseCase.Request(
            userAccountId = Arb.userAccountId().single(),
            title = Arb.courseTitle().single(),
            maxParticipants = Arb.long(10L..100).single(),
            price = Arb.long(1000L..50000).single(),
        )

        val actual: Either<CourseException, CreateCourseUseCase.Response> = sut.create(givenRequest)

        withClue("COR1001 에러코드 응답") {
            actual.leftOrNull()
                .shouldBeInstanceOf<CourseException.InvalidUser>()
                .should {
                    it.errorCode.code shouldBe "COR1001"
                }
        }
    }

    context("error handling") {
        test("user-account repository 에서 발생한 경우, WLB000 에러 응답") {
            val unknownException = RuntimeException("unknown repository exception")
            val mockUserAccountRepository: UserAccountRepository = mockk {
                every { findById(any()) } throws unknownException
            }

            val courseRepositoryStub = CourseRepositoryStub(initialData = null)
            val sut = CreateCourseUseCase(fixedClock, mockUserAccountRepository, courseRepositoryStub)

            val givenRequest = CreateCourseUseCase.Request(
                userAccountId = Arb.userAccountId().single(),
                title = Arb.courseTitle().single(),
                maxParticipants = Arb.long(10L..100).single(),
                price = Arb.long(1000L..50000).single(),
            )

            val actual: Either<CourseException, CreateCourseUseCase.Response> = sut.create(givenRequest)

            withClue("내부 에러로 인한 WLB0000 에러코드 응답") {
                actual.leftOrNull()
                    .shouldBeInstanceOf<CourseException.ExternalServiceUnavailable>()
                    .should {
                        it.errorCode.code shouldBe "WLB0000"
                        it.cause shouldBe unknownException
                    }
            }
        }

        test("course repository 에서 발생한 경우, WLB000 에러 응답") {
            val instructorUser: UserAccount = Arb.instructorUserAccount().single()
            val userAccountRepositoryStub = UserAccountRepositoryStub(initialData = listOf(instructorUser))

            val unknownException = RuntimeException("unknown repository exception")
            val mockCourseRepository: CourseRepository = mockk {
                every { saveNewCourse(any(), any(), any(), any()) } throws unknownException
            }

            val sut = CreateCourseUseCase(fixedClock, userAccountRepositoryStub, mockCourseRepository)

            val givenRequest = CreateCourseUseCase.Request(
                userAccountId = instructorUser.id,
                title = Arb.courseTitle().single(),
                maxParticipants = Arb.long(10L..100).single(),
                price = Arb.long(1000L..50000).single(),
            )

            val actual: Either<CourseException, CreateCourseUseCase.Response> = sut.create(givenRequest)

            withClue("내부 에러로 인한 WLB0000 에러코드 응답") {
                actual.leftOrNull()
                    .shouldBeInstanceOf<CourseException.ExternalServiceUnavailable>()
                    .should {
                        it.errorCode.code shouldBe "WLB0000"
                        it.cause shouldBe unknownException
                    }
            }
        }
    }
})
