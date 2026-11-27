package de.baupilot.backend

import android.content.SharedPreferences
import io.appwrite.ID
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** The three push-target calls of [Account]; a seam so the registrar is testable without the SDK's final class. */
interface PushTargetApi {
    suspend fun create(targetId: String, token: String, providerId: String)

    suspend fun update(targetId: String, token: String)

    suspend fun delete(targetId: String)
}

internal class AccountPushTargetApi(private val account: Account) : PushTargetApi {
    override suspend fun create(targetId: String, token: String, providerId: String) {
        account.createPushTarget(targetId, token, providerId)
    }

    override suspend fun update(targetId: String, token: String) {
        account.updatePushTarget(targetId, token)
    }

    override suspend fun delete(targetId: String) {
        account.deletePushTarget(targetId)
    }
}

class AppwritePushRegistrar(
    private val api: PushTargetApi,
    private val prefs: SharedPreferences,
    private val tokenSource: suspend () -> String?,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : PushRegistrar {
    constructor(
        account: Account,
        prefs: SharedPreferences,
        tokenSource: suspend () -> String?,
    ) : this(AccountPushTargetApi(account), prefs, tokenSource)

    override suspend fun register(userId: String) {
        val token = tokenSource() ?: prefs.getString(KEY_TOKEN, null) ?: return
        val targetId = prefs.getString(KEY_TARGET, null)
        if (targetId == null) {
            val id = ID.unique()
            api.create(id, token, PROVIDER_FCM)
            prefs.edit().putString(KEY_TARGET, id).putString(KEY_TOKEN, token).apply()
        } else if (prefs.getString(KEY_TOKEN, null) != token) {
            api.update(targetId, token)
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }
    }

    override fun onNewToken(token: String) {
        val targetId = prefs.getString(KEY_TARGET, null)
        if (prefs.getString(KEY_TOKEN, null) == token) return
        if (targetId == null) {
            prefs.edit().putString(KEY_TOKEN, token).apply()
            return
        }
        scope.launch {
            runCatching { api.update(targetId, token) }.onSuccess {
                prefs.edit().putString(KEY_TOKEN, token).apply()
            }
        }
    }

    override suspend fun unregister() {
        val targetId = prefs.getString(KEY_TARGET, null) ?: return
        runCatching { api.delete(targetId) }
        prefs.edit().remove(KEY_TARGET).remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val PROVIDER_FCM = "fcm"
        const val KEY_TARGET = "push.targetId"
        const val KEY_TOKEN = "push.token"
    }
}
