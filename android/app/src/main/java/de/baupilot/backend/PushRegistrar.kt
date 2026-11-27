package de.baupilot.backend

interface PushRegistrar {
    suspend fun register(userId: String)

    fun onNewToken(token: String)

    suspend fun unregister()
}

object NoPush : PushRegistrar {
    override suspend fun register(userId: String) = Unit

    override fun onNewToken(token: String) = Unit

    override suspend fun unregister() = Unit
}
