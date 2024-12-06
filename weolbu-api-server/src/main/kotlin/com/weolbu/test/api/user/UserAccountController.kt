package com.weolbu.test.api.user

import arrow.core.getOrElse
import com.weolbu.test.user.domain.UserInformation
import com.weolbu.test.user.domain.UserType
import com.weolbu.test.user.usecase.CreateUserAccountUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAccountController(
    private val useCase: CreateUserAccountUseCase,
) {
    data class Request(
        val name: String,
        val email: String,
        val phoneNumber: String,
        val userType: UserType,
        val password: String,
    )

    data class Response(
        val userAccountId: Long,
    )

    @PostMapping("/user-accounts")
    fun createUserAccount(@RequestBody request: Request): ResponseEntity<Response> {
        return useCase.create(
            CreateUserAccountUseCase.Request(
                userInformation = UserInformation(
                    name = request.name,
                    email = request.email,
                    phoneNumber = request.phoneNumber,
                    userType = request.userType,
                ),
                password = request.password,
            ),
        )
            .map { ResponseEntity.ok(Response(it.userAccountId)) }
            .getOrElse { throw it }
    }
}
