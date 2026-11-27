package de.baupilot.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * KEEP, not APPEND_OR_REPLACE: appending builds an ever-growing chain while offline, and ops added during a run
 * are caught by the engine's next round.
 */
class WorkManagerSyncScheduler(context: Context) : SyncScheduler {
    private val appContext = context.applicationContext

    private val connected: Constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(connected)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(ONE_TIME_NAME, ExistingWorkPolicy.KEEP, request)
    }

    override fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(PERIOD_HOURS, TimeUnit.HOURS)
            .setConstraints(connected)
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override fun cancelAll() {
        val manager = WorkManager.getInstance(appContext)
        manager.cancelUniqueWork(ONE_TIME_NAME)
        manager.cancelUniqueWork(PERIODIC_NAME)
    }

    companion object {
        const val ONE_TIME_NAME = "baupilot-sync"
        const val PERIODIC_NAME = "baupilot-sync-periodic"
        const val BACKOFF_SECONDS = 30L
        const val PERIOD_HOURS = 6L
    }
}
