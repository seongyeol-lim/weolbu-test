plugins {
    id("buildlogic.kotlin-spring-boot-conventions")

    kotlin("plugin.jpa")
    kotlin("kapt")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    api(project(":weolbu-core"))

    implementation(platform(libs.spring.boot.dependencies))

    // Starter for using Spring Data JPA with Hibernate
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // MySQL Driver
    runtimeOnly("com.mysql:mysql-connector-j")

    // h2 embedded database
    runtimeOnly("com.h2database:h2")

    // APT based Source code generation for Querydsl
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")

    // JPA support for Querydsl
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")

    testImplementation(testFixtures(project(":weolbu-core")))

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
    testImplementation(libs.kotest.extensions.spring)
}
