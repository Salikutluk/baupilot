package de.baupilot.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import de.baupilot.MainActivity
import de.baupilot.R

class ReminderNotifier(private val context: Context) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        manager().createNotificationChannel(channel)
    }

    fun show(p: ReminderPayload) {
        ensureChannel()
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_TARGET, p.target)
        val content = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.stat_reminder)
            .setContentTitle(p.title)
            .setContentText(p.body)
            .setContentIntent(content)
            .setAutoCancel(true)
            .build()
        manager().notify(NOTIFICATION_ID, notification)
    }

    private fun manager() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "deadlines"
        const val CHANNEL_NAME = "Fristen-Erinnerungen"
        const val EXTRA_TARGET = "de.baupilot.target"
        private const val NOTIFICATION_ID = 1001
    }
}
