package com.weolbu.test.infra.database.course

import com.weolbu.test.infra.database.WeolbuJpaConfiguration
import com.weolbu.test.user.domain.userAccountId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.single
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@SpringBootTest(
    classes = [WeolbuJpaConfiguration::class],
    properties = [
        "spring.config.import=classpath:/config-datasource.yml",
        "logging.level.root=DEBUG",
    ],
)
class CourseRegistrationJpaRepositoryTest(
    private val repository: CourseRegistrationJpaRepository,
) : FunSpec({
    test("CRD Test") {
        val newEntity = repository.save(
            CourseRegistrationEntity(
                id = null,
                userAccountId = Arb.userAccountId().single(),
                courseId = Arb.long().single(),
                createdAt = LocalDateTime.now(),
            ),
        )

        val newEntityId: Long = newEntity.id!!

        repository.findById(newEntityId).getOrNull().shouldNotBeNull {
            id shouldBe newEntityId
        }

        repository.deleteById(newEntityId)

        repository.findById(newEntityId).isEmpty shouldBe true
    }
})
