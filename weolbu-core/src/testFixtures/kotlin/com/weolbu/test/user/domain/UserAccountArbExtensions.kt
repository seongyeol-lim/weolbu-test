package com.weolbu.test.user.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.stringPattern

fun Arb.Companion.userAccountId(): Arb<Long> {
    return Arb.long(1..Long.MAX_VALUE)
}

fun Arb.Companion.userName(): Arb<String> {
    // 테스트에 사용할 데이터. https://koreanname.me/
    val list = listOf("홍길동", "김민준", "김서준", "김도윤", "김예준", "김시우", "김하준", "김지호", "김주원", "김지우")
    return Arb.of(list)
}

fun Arb.Companion.email(): Arb<String> {
    return Arb.stringPattern("""^[a-z]{3,8}$""").map { "$it@fake.gmail.com" }
}

fun Arb.Companion.phoneNumber(): Arb<String> {
    return Arb.stringPattern("""^[0-9]{7,8}$""").map { "010$it" }
}

fun Arb.Companion.userType(): Arb<UserType> {
    return Arb.of(UserType.entries)
}

fun Arb.Companion.passwordText(): Arb<String> {
    return Arb.bind(
        Arb.list(Arb.char('a'..'z'), 2..3),
        Arb.list(Arb.char('A'..'Z'), 2..3),
        Arb.list(Arb.char('0'..'9'), 2..3),
    ) { uppercase: List<Char>, lowercase: List<Char>, digit: List<Char> ->
        listOf(uppercase, lowercase, digit).flatten().shuffled().joinToString("")
    }
}

fun Arb.Companion.userPassword(): Arb<UserPassword> {
    return passwordText().map { UserPassword.create(it).getOrNull()!! }
}

fun Arb.Companion.userInformation(
    arbName: Arb<String> = userName(),
    arbEmail: Arb<String> = email(),
    arbPhoneNumber: Arb<String> = phoneNumber(),
    arbUserType: Arb<UserType> = userType(),
): Arb<UserInformation> {
    return Arb.bind(
        arbName,
        arbEmail,
        arbPhoneNumber,
        arbUserType,
    ) { name: String, email: String, phoneNumber: String, userType: UserType ->
        UserInformation(name = name, email = email, phoneNumber = phoneNumber, userType = userType)
    }
}

fun Arb.Companion.userAccount(
    arbUserAccountId: Arb<Long> = userAccountId(),
    arbUserInformation: Arb<UserInformation> = userInformation(),
    arbUserPassword: Arb<UserPassword> = userPassword(),
): Arb<UserAccount> {
    return Arb.bind(
        arbUserAccountId,
        arbUserInformation,
        arbUserPassword,
    ) { userAccountId: Long, userInformation: UserInformation, password: UserPassword ->
        UserAccount(id = userAccountId, userInformation = userInformation, password = password)
    }
}

fun Arb.Companion.instructorUserAccount(): Arb<UserAccount> {
    return Arb.userAccount(
        arbUserInformation = Arb.userInformation(arbUserType = Arb.constant(UserType.INSTRUCTOR)),
    )
}

fun Arb.Companion.studentUserAccount(): Arb<UserAccount> {
    return Arb.userAccount(
        arbUserInformation = Arb.userInformation(arbUserType = Arb.constant(UserType.STUDENT)),
    )
}
