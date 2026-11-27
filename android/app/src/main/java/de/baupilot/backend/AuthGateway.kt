package de.baupilot.backend

sealed interface LoginResult {
    data class Success(val userId: String) : LoginResult
    data class Refused(val reason: LoginRefusal) : LoginResult
}

enum class LoginRefusal { InvalidCredentials, Offline, RateLimited, NotConfigured, Unknown }

interface AuthGateway {
    val cachedUserId: String?
    val cachedRole: String?

    suspend fun logIn(email: String, password: String): LoginResult

    suspend fun logOut()

    fun clearCachedUser()
}

private val ROLE_PRIORITY = listOf("bauherr", "architekt", "handwerker")

/** D25: the first of bauherr, architekt, handwerker among the user's team ids; null if none. */
fun resolveRole(teamIds: Collection<String>): String? = ROLE_PRIORITY.firstOrNull { it in teamIds }
