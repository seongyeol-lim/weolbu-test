plugins {
    id("buildlogic.kotlin-conventions")
}

dependencies {
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
}
