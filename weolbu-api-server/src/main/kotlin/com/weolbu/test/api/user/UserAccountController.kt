package com.weolbu.test.api.user

import arrow.core.getOrElse
import com.weolbu.test.user.domain.UserInformation
import com.weolbu.test.user.domain.UserType
import com.weolbu.test.user.usecase.CreateUserAccountUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "user-account", description = "회원 API")
@RestController
class UserAccountController(
    private val useCase: CreateUserAccountUseCase,
) {
    @Schema(name = "회원 계정 생성 API RequestBody")
    data class Request(
        val name: String,
        val email: String,
        val phoneNumber: String,
        val userType: UserType,
        val password: String,
    )

    @Schema(name = "회원 계정 생성 API ResponseBody")
    data class Response(
        val userAccountId: Long,
    )

    @PostMapping("/user-accounts")
    @Operation(summary = "회원 계정 생성 API", description = "회원 계정을 생성해요.")
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
