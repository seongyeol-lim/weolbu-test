package com.weolbu.test.api.user

import arrow.core.getOrElse
import com.weolbu.test.contract.apis.UserAccountsApi
import com.weolbu.test.contract.models.CreateUserAccount200Response
import com.weolbu.test.contract.models.CreateUserAccountRequest
import com.weolbu.test.user.domain.UserInformation
import com.weolbu.test.user.domain.UserType
import com.weolbu.test.user.usecase.CreateUserAccountUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAccountController(
    private val useCase: CreateUserAccountUseCase,
) : UserAccountsApi {
    override fun createUserAccount(request: CreateUserAccountRequest): ResponseEntity<CreateUserAccount200Response> {
        return useCase.create(
            CreateUserAccountUseCase.Request(
                userInformation = UserInformation(
                    name = request.name,
                    email = request.email,
                    phoneNumber = request.phoneNumber,
                    userType = request.userType.toDomainType(),
                ),
                password = request.password,
            ),
        )
            .map { ResponseEntity.ok(CreateUserAccount200Response(it.userAccountId)) }
            .getOrElse { throw it }
    }

    private fun CreateUserAccountRequest.UserType.toDomainType(): UserType {
        return when (this) {
            CreateUserAccountRequest.UserType.INSTRUCTOR -> UserType.INSTRUCTOR
            CreateUserAccountRequest.UserType.STUDENT -> UserType.STUDENT
        }
    }
}
