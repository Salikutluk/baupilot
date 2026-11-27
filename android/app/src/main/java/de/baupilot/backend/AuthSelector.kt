package de.baupilot.backend

object AuthSelector {
    fun select(config: BackendConfig, isDebug: Boolean, appwrite: () -> AuthGateway): AuthGateway = when {
        config.isConfigured -> appwrite()
        isDebug -> DemoAuth()
        else -> UnconfiguredAuth()
    }
}
