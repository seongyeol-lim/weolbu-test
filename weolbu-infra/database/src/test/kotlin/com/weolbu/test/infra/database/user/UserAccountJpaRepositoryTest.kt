package com.weolbu.test.infra.database.user

import com.weolbu.test.infra.database.WeolbuJpaConfiguration
import com.weolbu.test.user.domain.email
import com.weolbu.test.user.domain.passwordText
import com.weolbu.test.user.domain.phoneNumber
import com.weolbu.test.user.domain.userName
import com.weolbu.test.user.domain.userType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import org.springframework.boot.test.context.SpringBootTest
import kotlin.jvm.optionals.getOrNull

@SpringBootTest(
    classes = [WeolbuJpaConfiguration::class],
    properties = [
        "spring.config.import=classpath:/config-datasource.yml",
        "logging.level.root=DEBUG",
    ],
)
class UserAccountJpaRepositoryTest(
    private val repository: UserAccountJpaRepository,
) : FunSpec({
    test("CRD Test") {
        val newEntity = repository.save(
            UserAccountEntity(
                id = null,
                name = Arb.userName().single(),
                email = Arb.email().single(),
                phoneNumber = Arb.phoneNumber().single(),
                userType = Arb.userType().single(),
                passwordDigest = Arb.passwordText().single(),
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
