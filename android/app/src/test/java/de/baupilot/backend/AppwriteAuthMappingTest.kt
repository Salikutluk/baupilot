package de.baupilot.backend

import io.appwrite.exceptions.AppwriteException
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppwriteAuthMappingTest {
    private fun appwrite(code: Int?) = AppwriteException("m", code, "t", "")

    @Test
    fun invalidCredentials() {
        assertEquals(LoginRefusal.InvalidCredentials, refusalFor(appwrite(401)))
        assertEquals(LoginRefusal.InvalidCredentials, refusalFor(appwrite(400)))
    }

    @Test
    fun rateLimited() {
        assertEquals(LoginRefusal.RateLimited, refusalFor(appwrite(429)))
    }

    @Test
    fun ioIsOffline() {
        assertEquals(LoginRefusal.Offline, refusalFor(IOException("down")))
        assertEquals(LoginRefusal.Offline, refusalFor(UnknownHostException("h")))
        assertEquals(LoginRefusal.Offline, refusalFor(RuntimeException(IOException("wrapped"))))
    }

    @Test
    fun everythingElseIsUnknown() {
        assertEquals(LoginRefusal.Unknown, refusalFor(appwrite(500)))
        assertEquals(LoginRefusal.Unknown, refusalFor(appwrite(null)))
        assertEquals(LoginRefusal.Unknown, refusalFor(IllegalStateException()))
    }

    @Test
    fun staleSessionIsDroppedOnceAndTheLoginRetried() = runBlocking {
        var attempts = 0
        var dropped = 0
        val userId = retryAfterStaleSession(
            attempt = {
                attempts++
                if (attempts == 1) throw AppwriteException("m", 401, "user_session_already_exists", "") else "u1"
            },
            dropSession = { dropped++ },
        )
        assertEquals("u1", userId)
        assertEquals(2, attempts)
        assertEquals(1, dropped)
    }

    @Test
    fun otherFailuresAreNotRetriedAndAFailingDropDoesNotMaskTheRetry() = runBlocking {
        var attempts = 0
        val failure = runCatching {
            retryAfterStaleSession(
                attempt = { attempts++; throw AppwriteException("m", 401, "user_invalid_credentials", "") },
                dropSession = { throw IOException("offline") },
            )
        }.exceptionOrNull()
        assertEquals(1, attempts)
        assertEquals(InvalidCredentialsFor401, refusalFor(failure!!))

        var second = 0
        val again = runCatching {
            retryAfterStaleSession(
                attempt = { second++; throw AppwriteException("m", 401, "user_session_already_exists", "") },
                dropSession = { throw IOException("offline") },
            )
        }.exceptionOrNull()
        assertEquals(2, second)
        assertTrue(again is AppwriteException)
    }

    private companion object {
        val InvalidCredentialsFor401 = LoginRefusal.InvalidCredentials
    }
}
