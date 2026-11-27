package de.baupilot.backend

import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSelectorTest {
    private val configured = BackendConfig("proj", null)
    private val unconfigured = BackendConfig("", null)
    private val appwrite = DemoAuth()

    @Test
    fun configuredUsesAppwriteInRelease() {
        assertSame(appwrite, AuthSelector.select(configured, isDebug = false) { appwrite })
    }

    @Test
    fun configuredUsesAppwriteInDebug() {
        assertSame(appwrite, AuthSelector.select(configured, isDebug = true) { appwrite })
    }

    @Test
    fun unconfiguredDebugIsDemo() {
        val auth = AuthSelector.select(unconfigured, isDebug = true) { error("must not build Appwrite") }
        assertTrue(auth is DemoAuth)
    }

    @Test
    fun unconfiguredReleaseRefuses() {
        val auth = AuthSelector.select(unconfigured, isDebug = false) { error("must not build Appwrite") }
        assertTrue(auth is UnconfiguredAuth)
    }
}
