plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.paparazzi)
}

val repoRoot = rootProject.projectDir.parentFile

android {
    namespace = "de.baupilot"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.baupilot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    buildFeatures {
        compose = true
    }

    sourceSets["main"].assets.directories.add(File(repoRoot, "fixtures").path)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    systemProperty("baupilot.repoRoot", repoRoot.path)
    inputs.dir(File(repoRoot, "design/reference/android"))
    inputs.file(File(repoRoot, "fixtures/baupilot.json"))
}
