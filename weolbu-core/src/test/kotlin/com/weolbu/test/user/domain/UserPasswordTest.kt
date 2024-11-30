package com.weolbu.test.user.domain

import arrow.core.Either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.merge
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

class UserPasswordTest : FunSpec({
    test("비밀번호 정책에 유효한 경우 -> UserPassword 생성 성공") {
        checkAll(Arb.passwordText()) { passwordText: String ->
            println(passwordText)

            val userPassword: UserPassword = UserPassword.create(passwordText).getOrNull()!!
            userPassword.matches(passwordText) shouldBe true
        }
    }

    context("비밀번호 정책 위반 테스트") {
        test("비밀번호 길이가 6자리 미만인 경우 -> PasswordPolicy.PASSWORD_LENGTH") {
            val arbInvalidPassword: Arb<String> = Arb.stringPattern("""[a-zA-Z0-9]{1,5}""")
            checkAll(iterations = 1000, arbInvalidPassword) { passwordText ->
                println(passwordText)

                val actual: Either<PasswordPolicy, UserPassword> = UserPassword.create(passwordText)
                actual.leftOrNull() shouldBe PasswordPolicy.PASSWORD_LENGTH
            }
        }

        test("비밀번호 길이가 10자리 초과인 경우 -> PasswordPolicy.PASSWORD_LENGTH") {
            val arbInvalidPassword: Arb<String> = Arb.stringPattern("""[a-zA-Z0-9]{11,20}""")
            checkAll(iterations = 1000, arbInvalidPassword) { passwordText ->
                println(passwordText)

                val actual: Either<PasswordPolicy, UserPassword> = UserPassword.create(passwordText)
                actual.leftOrNull() shouldBe PasswordPolicy.PASSWORD_LENGTH
            }
        }

        test("한 가지 조합으로만 구성된 경우 -> PasswordPolicy.CHARACTER_COMBINATION") {
            val arbOnlyLowercase: Arb<String> = Arb.stringPattern("""[a-z]{6,10}""")
            val arbOnlyUppercase: Arb<String> = Arb.stringPattern("""[A-Z]{6,10}""")
            val arbOnlyDigit: Arb<String> = Arb.stringPattern("""[0-9]{6,10}""")

            val arbInvalidPassword: Arb<String> = arbOnlyLowercase.merge(arbOnlyUppercase).merge(arbOnlyDigit)

            checkAll(iterations = 1000, arbInvalidPassword) { passwordText ->
                println(passwordText)

                val actual: Either<PasswordPolicy, UserPassword> = UserPassword.create(passwordText)
                actual.leftOrNull() shouldBe PasswordPolicy.CHARACTER_COMBINATION
            }
        }
    }
})
