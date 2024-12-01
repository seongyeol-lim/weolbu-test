package com.weolbu.test.api

import com.weolbu.test.adapter.WeolbuAdapterConfiguration
import com.weolbu.test.course.domain.CourseRepository
import com.weolbu.test.course.usecase.CreateCourseUseCase
import com.weolbu.test.course.usecase.ListCourseUseCase
import com.weolbu.test.course.usecase.RegisterCourseUseCase
import com.weolbu.test.user.domain.UserAccountRepository
import com.weolbu.test.user.usecase.CreateUserAccountUseCase
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock

@SpringBootApplication
class WeolbuApiApplication

fun main(args: Array<String>) {
    runApplication<WeolbuApiApplication>(*args)
}

@Configuration
@Import(WeolbuAdapterConfiguration::class)
class WeolbuAppApplicationConfiguration {
    @Bean
    fun createUserAccountUseCase(
        userAccountRepository: UserAccountRepository,
    ): CreateUserAccountUseCase {
        return CreateUserAccountUseCase(userAccountRepository)
    }

    @Bean
    fun createCourseUseCase(
        clock: Clock,
        userAccountResponse: UserAccountRepository,
        courseRepository: CourseRepository,
    ): CreateCourseUseCase {
        return CreateCourseUseCase(clock, userAccountResponse, courseRepository)
    }

    @Bean
    fun listCourseUseCase(courseRepository: CourseRepository): ListCourseUseCase {
        return ListCourseUseCase(courseRepository)
    }

    @Bean
    fun registerCourseUseCase(
        clock: Clock,
        userAccountResponse: UserAccountRepository,
        courseRepository: CourseRepository,
    ): RegisterCourseUseCase {
        return RegisterCourseUseCase(clock, userAccountResponse, courseRepository)
    }

    @Bean
    fun clock(): Clock {
        return Clock.systemDefaultZone()
    }
}
