package de.baupilot.push

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ReminderNotifierTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Test
    fun channelIsDeadlinesWithDefaultImportance() {
        ReminderNotifier(context).ensureChannel()
        val channel = manager.getNotificationChannel("deadlines")
        assertNotNull(channel)
        assertEquals("Fristen-Erinnerungen", channel.name.toString())
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
    }

    @Test
    fun showPostsTitleBodyAndTargetIntent() {
        ReminderNotifier(context).show(
            ReminderPayload("Fristen-Erinnerung", "2 Mängel mit Frist bis 20.12.2026", "defects", "p1"),
        )
        val posted = shadowOf(manager).allNotifications
        assertEquals(1, posted.size)
        val n = posted.single()
        assertEquals("deadlines", n.channelId)
        assertEquals("Fristen-Erinnerung", n.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("2 Mängel mit Frist bis 20.12.2026", n.extras.getString(Notification.EXTRA_TEXT))
        assertTrue(n.flags and Notification.FLAG_AUTO_CANCEL != 0)

        val intent = shadowOf(n.contentIntent).savedIntent
        assertEquals("de.baupilot.MainActivity", intent.component?.className)
        assertEquals("defects", intent.getStringExtra("de.baupilot.target"))
    }
}
