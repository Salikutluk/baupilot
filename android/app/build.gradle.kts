import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

val repoRoot = rootProject.projectDir.parentFile

// D10: backend identifiers come from Gradle properties or android/local.properties, never from git.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}

fun backendProperty(name: String): String =
    providers.gradleProperty(name).orNull ?: localProperties.getProperty(name) ?: ""

fun String.asJavaStringLiteral(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\""

android {
    namespace = "de.baupilot"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.baupilot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"

        buildConfigField("String", "APPWRITE_PROJECT_ID", backendProperty("baupilot.appwrite.projectId").asJavaStringLiteral())
        buildConfigField("String", "FCM_APPLICATION_ID", backendProperty("baupilot.fcm.applicationId").asJavaStringLiteral())
        buildConfigField("String", "FCM_API_KEY", backendProperty("baupilot.fcm.apiKey").asJavaStringLiteral())
        buildConfigField("String", "FCM_PROJECT_ID", backendProperty("baupilot.fcm.projectId").asJavaStringLiteral())
        buildConfigField("String", "FCM_SENDER_ID", backendProperty("baupilot.fcm.senderId").asJavaStringLiteral())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    sourceSets["main"].assets.directories.add(File(repoRoot, "fixtures").path)
}

kotlin {
    jvmToolchain(21)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.work.runtime.ktx)
    implementation(libs.appwrite)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.room.testing)
    testImplementation(libs.work.testing)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.withType<Test> {
    systemProperty("baupilot.repoRoot", repoRoot.path)
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
    inputs.dir(File(repoRoot, "design/reference/android"))
    inputs.file(File(repoRoot, "fixtures/baupilot.json"))
    inputs.files(File(repoRoot, "backend/appwrite/appwrite.config.json")).withPropertyName("appwriteConfig")
    inputs.files(fileTree(File(projectDir, "src/test/goldens"))).withPropertyName("goldens")
    val record = providers.gradleProperty("baupilot.recordGoldens").getOrElse("false")
    systemProperty("baupilot.recordGoldens", record)
    inputs.property("baupilot.recordGoldens", record)
    if (record == "true") outputs.upToDateWhen { false }
}

tasks.withType<Test>().matching { it.name.endsWith("UnitTest") }.configureEach {
    forkEvery = 1
}
