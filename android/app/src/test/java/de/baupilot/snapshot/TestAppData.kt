package de.baupilot.snapshot

import de.baupilot.app.AppData
import de.baupilot.data.MockRepository
import java.io.File
import kotlinx.coroutines.runBlocking

/** Screen models for snapshot tests, built from the repository fixture (`baupilot.repoRoot`). */
object TestAppData {
    fun load(): AppData {
        val fixture = File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()
        return runBlocking { AppData.load(MockRepository(fixture)) }
    }
}
