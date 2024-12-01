plugins {
    id("buildlogic.kotlin-spring-boot-conventions")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":weolbu-core"))
    implementation(project(":weolbu-infra:database"))

    implementation(platform(libs.spring.boot.dependencies))

    // Core starter, including auto-configuration support, logging and YAML
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
}
