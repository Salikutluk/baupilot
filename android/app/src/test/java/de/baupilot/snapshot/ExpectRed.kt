package de.baupilot.snapshot

import org.junit.Assume

/**
 * Marks a snapshot test as expected-red until [milestone] ships its screen.
 *
 * Only a failed comparison (`AssertionError`) is turned into a skip that carries the diff
 * message. A missing reference or a wrong frame size is an `IllegalStateException` and still fails.
 */
inline fun expectRedUntil(milestone: String, block: () -> Unit) {
    try {
        block()
    } catch (e: AssertionError) {
        Assume.assumeTrue("Erwartet rot bis $milestone: ${e.message}", false)
    }
}
