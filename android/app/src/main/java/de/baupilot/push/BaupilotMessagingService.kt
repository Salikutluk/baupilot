package de.baupilot.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.baupilot.BaupilotApplication

/** FCM receipt (plan §5.11): a new token goes to the registrar, a message becomes a reminder notification. */
class BaupilotMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        (applicationContext as BaupilotApplication).services.push.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification
        val payload = ReminderPayloads.parse(message.data, notification?.title, notification?.body) ?: return
        ReminderNotifier(this).show(payload)
    }
}
