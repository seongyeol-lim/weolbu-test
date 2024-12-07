plugins {
    id("buildlogic.kotlin-spring-boot-conventions")
    alias(libs.plugins.openapi.generator)
}

val openApiSpec = File(rootDir, "weolbu-api.oas.yaml")
val taskCopyOpenApiSpec = project.task<Copy>("copyOpenApiSpec") {
    from(openApiSpec.parentFile) {
        include("*.oas.yaml")
    }
    into("src/main/resources/static/openapi")
}

tasks.processResources {
    dependsOn(taskCopyOpenApiSpec)
}

// Validating a single specification
openApiValidate {
    inputSpec.set(openApiSpec.absolutePath)
}

// https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(openApiSpec.absolutePath)
    outputDir.set("${project.layout.buildDirectory.get()}/generated")
    packageName.set("com.weolbu.test.contract")
    configOptions.set(
        // https://openapi-generator.tech/docs/generators/kotlin-spring
        // https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/kotlin-spring.md
        mapOf(
            "annotationLibrary" to "none",
            "documentationProvider" to "none",
            "apiSuffix" to "Api",
            "enumPropertyNaming" to "UPPERCASE",
            "exceptionHandler" to "false",
            "gradleBuildFile" to "false",
            "interfaceOnly" to "true",
            "serializableModel" to "false",
            "skipDefaultInterface" to "true",
            "useSpringBoot3" to "true",
            "useTag" to "true",
            "jackson" to "false",
            "useBeanValidation" to "false",
        ),
    )
    additionalProperties.set(mapOf())
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

sourceSets.main {
    kotlin.srcDir("${project.layout.buildDirectory.get()}/generated")
}

tasks.runKtlintCheckOverMainSourceSet {
    dependsOn(tasks.openApiGenerate)
}

ktlint {
    filter {
        /*
         * I could not filter dynamically attached sources that are located outside of the project dir.
         * https://github.com/JLLeitschuh/ktlint-gradle?tab=readme-ov-file#faq
         */
        exclude { element -> element.file.path.contains("generated") }
    }
}

dependencies {
    implementation(project(":weolbu-core"))
    implementation(project(":weolbu-adapter"))

    implementation(platform(libs.spring.boot.dependencies))

    // Starter for building web, including RESTful, applications using Spring MVC.
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
}
