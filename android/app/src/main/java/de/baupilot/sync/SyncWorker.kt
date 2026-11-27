package de.baupilot.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

/**
 * Where the worker finds its engine. The application sets [provider] at start-up; the worker cannot take the
 * engine as a constructor argument because WorkManager builds it from `(Context, WorkerParameters)` only.
 */
object SyncEngineRegistry {
    @Volatile
    var provider: ((Context) -> SyncEngine?)? = null
}

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val engine = SyncEngineRegistry.provider?.invoke(applicationContext) ?: return Result.retry()
        return resultFor(engine.drain())
    }

    companion object {
        /** Done, NotLoggedIn and NotConfigured end the work; Retry backs off; AuthRequired stops until a new login. */
        fun resultFor(outcome: SyncOutcome): ListenableWorker.Result = when (outcome) {
            is SyncOutcome.Done, SyncOutcome.NotLoggedIn, SyncOutcome.NotConfigured -> ListenableWorker.Result.success()
            is SyncOutcome.Retry -> ListenableWorker.Result.retry()
            SyncOutcome.AuthRequired -> ListenableWorker.Result.failure()
        }
    }
}
