plugins {
    id("buildlogic.kotlin-conventions")
    `java-test-fixtures`
}

dependencies {
    api(libs.arrowkt.core)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)

    testFixturesApi(libs.kotest.property)
}
