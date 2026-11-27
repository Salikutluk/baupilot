package de.baupilot.sync

/** Hands sync work to the platform scheduler (plan §5.8). */
interface SyncScheduler {
    fun requestSync()

    fun schedulePeriodic()

    fun cancelAll()
}
