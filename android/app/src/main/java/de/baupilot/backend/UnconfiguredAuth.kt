package de.baupilot.backend

/** Release build without a backend project: nobody gets in (D11). */
class UnconfiguredAuth : AuthGateway {
    override val cachedUserId: String? = null
    override val cachedRole: String? = null

    override suspend fun logIn(email: String, password: String): LoginResult =
        LoginResult.Refused(LoginRefusal.NotConfigured)

    override suspend fun logOut() = Unit

    override fun clearCachedUser() = Unit
}
