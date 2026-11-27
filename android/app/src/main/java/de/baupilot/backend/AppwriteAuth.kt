package de.baupilot.backend

import android.content.SharedPreferences
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Teams
import java.io.IOException
import kotlinx.coroutines.CancellationException

/** Pure mapping of a login failure to a refusal (§5.9). */
fun refusalFor(error: Throwable): LoginRefusal {
    var cause: Throwable? = error
    while (cause != null) {
        if (cause is IOException) return LoginRefusal.Offline
        if (cause is AppwriteException) {
            return when (cause.code) {
                401, 400 -> LoginRefusal.InvalidCredentials
                429 -> LoginRefusal.RateLimited
                else -> LoginRefusal.Unknown
            }
        }
        cause = cause.cause?.takeIf { it !== cause }
    }
    return LoginRefusal.Unknown
}

private const val TYPE_SESSION_EXISTS = "user_session_already_exists"

/**
 * Appwrite refuses a new session with 401 `user_session_already_exists` while the SDK still holds an old one: after an
 * offline sign-out, or a login that was cut off before it stored the user (M10). The stale session is dropped by
 * [dropSession] (its failure does not matter) and [attempt] is retried once, so the user is never locked out.
 */
internal suspend fun <T> retryAfterStaleSession(attempt: suspend () -> T, dropSession: suspend () -> Unit): T = try {
    attempt()
} catch (e: AppwriteException) {
    if (e.type != TYPE_SESSION_EXISTS) throw e
    try {
        dropSession()
    } catch (c: CancellationException) {
        throw c
    } catch (ignored: Exception) {
        // The retry below reports what is still wrong.
    }
    attempt()
}

/**
 * Email/password session against Appwrite. The password is only passed on to the SDK call;
 * it is neither logged nor stored.
 */
class AppwriteAuth(
    client: Client,
    private val prefs: SharedPreferences,
    private val push: PushRegistrar = NoPush,
) : AuthGateway {
    private val account = Account(client)
    private val teams = Teams(client)

    override val cachedUserId: String? get() = prefs.getString(KEY_USER, null)
    override val cachedRole: String? get() = prefs.getString(KEY_ROLE, null)

    override suspend fun logIn(email: String, password: String): LoginResult {
        val userId = try {
            retryAfterStaleSession(
                attempt = { account.createEmailPasswordSession(email, password).userId },
                dropSession = { account.deleteSession(CURRENT_SESSION) },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return LoginResult.Refused(refusalFor(e))
        }
        val role = try {
            resolveRole(teams.list().teams.map { it.id })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        prefs.edit().putString(KEY_USER, userId).also { editor ->
            if (role == null) editor.remove(KEY_ROLE) else editor.putString(KEY_ROLE, role)
        }.apply()
        return LoginResult.Success(userId)
    }

    override suspend fun logOut() {
        runCatching { push.unregister() }
        try {
            account.deleteSession(CURRENT_SESSION)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline sign-out still clears the device; the server session expires on its own.
        }
        clearCachedUser()
    }

    override fun clearCachedUser() {
        prefs.edit().remove(KEY_USER).remove(KEY_ROLE).apply()
    }

    private companion object {
        const val CURRENT_SESSION = "current"
        const val KEY_USER = "auth.userId"
        const val KEY_ROLE = "auth.role"
    }
}
