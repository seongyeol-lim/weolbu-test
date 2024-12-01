package com.weolbu.test.infra.database

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.ZoneOffset

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories
class WeolbuJpaConfiguration

@Configuration
class JpaQueryFactoryConfiguration {
    @PersistenceContext
    lateinit var entityManager: EntityManager

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}

/** weolbu DB 속성값 */
object WeolbuDataSource {
    val ZONE_OFFSET: ZoneOffset = ZoneOffset.UTC
}
