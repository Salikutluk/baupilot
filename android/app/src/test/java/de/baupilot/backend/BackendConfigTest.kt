package de.baupilot.backend

import java.io.File
import java.net.URI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendConfigTest {
    @Test
    fun endpointIsFrankfurt() {
        val uri = URI(BackendConfig.FRANKFURT_ENDPOINT)
        assertEquals("https", uri.scheme)
        assertEquals("fra.cloud.appwrite.io", uri.host)
        assertEquals(BackendConfig.FRANKFURT_ENDPOINT, BackendConfig("p", null).endpoint)
    }

    @Test
    fun blankProjectIdIsUnconfigured() {
        assertFalse(BackendConfig("", null).isConfigured)
        assertFalse(BackendConfig("   ", null).isConfigured)
        assertTrue(BackendConfig("abc", null).isConfigured)
    }

    @Test
    fun noOtherAppwriteEndpointInSources() {
        val main = File(System.getProperty("user.dir"), "src/main")
        assertTrue("src/main missing at $main", main.isDirectory)
        val offenders = main.walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "xml", "java") }
            .filter { it.readText().contains("cloud.appwrite.io") }
            .map { it.name }
            .toList()
        assertEquals(listOf("BackendConfig.kt"), offenders)
    }
}
