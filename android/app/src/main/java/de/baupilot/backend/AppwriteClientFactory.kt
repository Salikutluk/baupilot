package de.baupilot.backend

import android.content.Context
import io.appwrite.Client

/** Builds the one SDK client for the Frankfurt endpoint and the configured project. */
object AppwriteClientFactory {
    fun create(context: Context, config: BackendConfig): Client =
        Client(context.applicationContext)
            .setEndpoint(config.endpoint)
            .setProject(config.projectId)
}
