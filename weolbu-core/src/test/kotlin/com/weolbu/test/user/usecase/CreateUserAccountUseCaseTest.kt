package com.weolbu.test.user.usecase

import arrow.core.Either
import com.weolbu.test.user.domain.UserAccount
import com.weolbu.test.user.domain.UserAccountRepository
import com.weolbu.test.user.domain.UserAccountRepositoryStub
import com.weolbu.test.user.domain.passwordText
import com.weolbu.test.user.domain.userAccount
import com.weolbu.test.user.domain.userInformation
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.single
import io.mockk.every
import io.mockk.mockk

class CreateUserAccountUseCaseTest : FunSpec({
    test("사용자는 회원 정보를 입력하여 회원 가입을 할 수 있어요.") {
        val repositoryStub = UserAccountRepositoryStub(initialData = null)
        val sut = CreateUserAccountUseCase(repositoryStub)

        val givenRequest = CreateUserAccountUseCase.Request(
            userInformation = Arb.userInformation().single(),
            password = Arb.passwordText().single(),
        )

        val actual: Either<UserAccountException, CreateUserAccountUseCase.Response> = sut.create(givenRequest)

        val savedUserAccount: UserAccount = repositoryStub.single()

        withClue("요청이 성공적으로 처리되며, 생성된 userAccountId 를 응답해요") {
            actual.getOrNull() shouldBe CreateUserAccountUseCase.Response(userAccountId = savedUserAccount.id)
        }

        withClue("Repository 에 사용자 계정이 신규로 저장되어요") {
            savedUserAccount.should {
                it.userInformation shouldBe givenRequest.userInformation
                it.password.matches(givenRequest.password) shouldBe true
            }
        }
    }

    test("이미 가입된 email 을 입력한 경우 -> USR1001 에러코드를 응답해요.") {
        val alreadyRegisteredUser: UserAccount = Arb.userAccount().single()
        val registeredEmail: String = alreadyRegisteredUser.userInformation.email

        val repositoryStub = UserAccountRepositoryStub(initialData = listOf(alreadyRegisteredUser))
        val sut = CreateUserAccountUseCase(repositoryStub)

        val givenRequest = CreateUserAccountUseCase.Request(
            userInformation = Arb.userInformation(arbEmail = Arb.constant(registeredEmail)).single(),
            password = Arb.passwordText().single(),
        )

        val actual: Either<UserAccountException, CreateUserAccountUseCase.Response> = sut.create(givenRequest)

        withClue("USR1001 에러코드 응답") {
            actual.leftOrNull()
                .shouldBeInstanceOf<UserAccountException.AlreadyRegisteredEmail>()
                .should {
                    it.errorCode.code shouldBe "USR1001"
                }
        }
    }

    context("입력한 비밀번호가 비밀번호 정책 요구사항을 충족하지 못하는 경우 -> 비밀번호 에러코드(USR0002/USR0003)를 응답해요.") {
        data class TestData(val password: String, val expectedErrorCode: String)

        withData(
            ts = listOf(
                TestData("1", "USR1002"),
                TestData("12345", "USR1002"),
                TestData("123456789aZ", "USR1002"),
                TestData("123456", "USR1003"),
                TestData("abcdef", "USR1003"),
                TestData("ABCDEF", "USR1003"),
            ),
        ) { (password: String, expectedErrorCode: String) ->
            val repositoryStub = UserAccountRepositoryStub(initialData = null)
            val sut = CreateUserAccountUseCase(repositoryStub)

            val givenRequest = CreateUserAccountUseCase.Request(
                userInformation = Arb.userInformation().single(),
                password = password,
            )

            val actual: Either<UserAccountException, CreateUserAccountUseCase.Response> = sut.create(givenRequest)

            withClue("비밀번호($password) -> 에러코드($expectedErrorCode) 응답") {
                actual.leftOrNull()
                    .shouldBeInstanceOf<UserAccountException>()
                    .should {
                        it.errorCode.code shouldBe expectedErrorCode
                    }
            }
        }
    }

    context("error handling") {
        test("repository 에서 에러가 발생한 경우, WLB0000 에러 응답") {
            val unknownException = RuntimeException("unknown repository exception")
            val mockRepository: UserAccountRepository = mockk {
                every { findByEmail(any()) } throws unknownException
            }
            val sut = CreateUserAccountUseCase(mockRepository)

            val givenRequest = CreateUserAccountUseCase.Request(
                userInformation = Arb.userInformation().single(),
                password = Arb.passwordText().single(),
            )

            val actual: Either<UserAccountException, CreateUserAccountUseCase.Response> = sut.create(givenRequest)

            withClue("내부 에러로 인한 WLB0000 에러코드 응답") {
                actual.leftOrNull()
                    .shouldBeInstanceOf<UserAccountException.ExternalServiceUnavailable>()
                    .should {
                        it.errorCode.code shouldBe "WLB0000"
                        it.cause shouldBe unknownException
                    }
            }
        }
    }
})
