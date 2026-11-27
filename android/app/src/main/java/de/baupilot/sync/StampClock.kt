package de.baupilot.sync

import java.time.Clock

/** Hybrid clock: strictly increasing per process and always past every stamp seen from the server. */
class StampClock(
    private val clock: Clock,
    private val deviceId: String,
    private val maxSeen: () -> Long,
) {
    private var last = 0L

    @Synchronized
    fun next(): FieldStamp {
        val at = maxOf(clock.millis(), maxSeen() + 1, last + 1)
        last = at
        return FieldStamp(at, deviceId)
    }
}
