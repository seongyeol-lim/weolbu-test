package com.weolbu.test.user.domain

data class UserAccount(
    val id: Long,
    val userInformation: UserInformation,
    val password: UserPassword,
)

data class UserInformation(
    /** 회원 이름 */
    val name: String,

    /** 회원 이메일 */
    val email: String,

    /** 회원 휴대폰 번호 */
    val phoneNumber: String,

    /** 회원 유형 */
    val userType: UserType,
)

enum class UserType {
    /** 강사 */
    INSTRUCTOR,

    /** 수강생 */
    STUDENT,
}
