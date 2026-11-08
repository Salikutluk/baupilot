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

    testOptions {
        unitTests.isIncludeAndroidResources = true
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
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.withType<Test> {
    systemProperty("baupilot.repoRoot", repoRoot.path)
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
    inputs.dir(File(repoRoot, "design/reference/android"))
    inputs.file(File(repoRoot, "fixtures/baupilot.json"))
    inputs.files(fileTree(File(projectDir, "src/test/goldens"))).withPropertyName("goldens")
    val record = providers.gradleProperty("baupilot.recordGoldens").getOrElse("false")
    systemProperty("baupilot.recordGoldens", record)
    inputs.property("baupilot.recordGoldens", record)
    if (record == "true") outputs.upToDateWhen { false }
}

tasks.withType<Test>().matching { it.name.endsWith("UnitTest") }.configureEach {
    forkEvery = 1
}
