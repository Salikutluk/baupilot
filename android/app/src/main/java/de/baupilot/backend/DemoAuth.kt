package de.baupilot.backend

/** Accepts any input without suspending and persists nothing (D11). Debug-unconfigured builds and tests only. */
class DemoAuth(initialUser: String? = null, role: String? = null) : AuthGateway {
    override var cachedUserId: String? = initialUser
        private set
    override var cachedRole: String? = role
        private set

    override suspend fun logIn(email: String, password: String): LoginResult {
        cachedUserId = DEMO_USER
        return LoginResult.Success(DEMO_USER)
    }

    override suspend fun logOut() = clearCachedUser()

    override fun clearCachedUser() {
        cachedUserId = null
        cachedRole = null
    }

    private companion object {
        const val DEMO_USER = "demo"
    }
}
