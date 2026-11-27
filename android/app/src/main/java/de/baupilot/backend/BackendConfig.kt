package de.baupilot.backend

import de.baupilot.BuildConfig

data class BackendConfig(val projectId: String, val fcm: FcmConfig?) {
    val endpoint: String get() = FRANKFURT_ENDPOINT
    val isConfigured get() = projectId.isNotBlank()

    companion object {
        const val FRANKFURT_ENDPOINT = "https://fra.cloud.appwrite.io/v1"

        fun fromBuildConfig(): BackendConfig {
            val fcm = FcmConfig(
                applicationId = BuildConfig.FCM_APPLICATION_ID,
                apiKey = BuildConfig.FCM_API_KEY,
                projectId = BuildConfig.FCM_PROJECT_ID,
                senderId = BuildConfig.FCM_SENDER_ID,
            )
            val complete = listOf(fcm.applicationId, fcm.apiKey, fcm.projectId, fcm.senderId).all { it.isNotBlank() }
            return BackendConfig(BuildConfig.APPWRITE_PROJECT_ID, if (complete) fcm else null)
        }
    }
}

data class FcmConfig(val applicationId: String, val apiKey: String, val projectId: String, val senderId: String)
