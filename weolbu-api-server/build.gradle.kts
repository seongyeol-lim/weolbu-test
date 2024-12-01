plugins {
    id("buildlogic.kotlin-spring-boot-conventions")
}

dependencies {
    implementation(project(":weolbu-core"))
    implementation(project(":weolbu-adapter"))

    implementation(platform(libs.spring.boot.dependencies))

    // Starter for building web, including RESTful, applications using Spring MVC.
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
}
