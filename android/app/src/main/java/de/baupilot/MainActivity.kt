package de.baupilot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.baupilot.app.BaupilotRoot
import de.baupilot.app.NavigationTarget
import de.baupilot.push.ReminderNotifier
import de.baupilot.push.ReminderPayloads
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    /** A deep link waiting for [BaupilotRoot]; it is cleared once applied (the root applies it when logged in). */
    private val pendingTarget = MutableStateFlow<NavigationTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) pendingTarget.value = targetOf(intent)
        setContent { BaupilotRoot(pendingTarget = pendingTarget) }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetOf(intent)?.let { pendingTarget.value = it }
    }

    internal companion object {
        const val FCM_TARGET = "target"

        /**
         * The activity is exported, so the extra is untrusted: only the allow-listed `defects` is accepted, anything
         * else (also an extra of another type) is ignored without throwing. `RouteParser` throws on bad input and is
         * not used here.
         */
        fun targetOf(intent: Intent?): NavigationTarget? {
            // `de.baupilot.target` comes from the in-app notification, the plain `target` from a push that the system drew
            // while the app was in the background (FCM puts the message data into the launch intent's extras, T2/T7).
            val value = try {
                intent?.getStringExtra(ReminderNotifier.EXTRA_TARGET) ?: intent?.getStringExtra(FCM_TARGET)
            } catch (e: RuntimeException) {
                null
            }
            return if (value == ReminderPayloads.TARGET_DEFECTS) NavigationTarget.Defects else null
        }
    }
}
