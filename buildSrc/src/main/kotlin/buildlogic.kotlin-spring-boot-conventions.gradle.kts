plugins {
    id("buildlogic.kotlin-conventions")

    id("org.springframework.boot")
    kotlin("plugin.spring")
}

dependencies {
    // Core starter, including auto-configuration support, logging and YAML
    implementation("org.springframework.boot:spring-boot-starter")

    // Starter for testing Spring Boot applications with libraries including JUnit Jupiter, Hamcrest and Mockito
    // https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
