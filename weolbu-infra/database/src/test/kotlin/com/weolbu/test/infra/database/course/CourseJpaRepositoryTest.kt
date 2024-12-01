package com.weolbu.test.infra.database.course

import com.weolbu.test.course.domain.courseTitle
import com.weolbu.test.infra.database.WeolbuJpaConfiguration
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
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
class CourseJpaRepositoryTest(
    private val repository: CourseJpaRepository,
) : FunSpec({
    test("CRD Test") {
        val newEntity = repository.save(
            CourseEntity(
                id = null,
                title = Arb.courseTitle().single(),
                maxParticipants = Arb.int(5..100).single(),
                price = Arb.int(1000..90000).single(),
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
