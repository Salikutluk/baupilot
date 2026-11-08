package de.baupilot.app

import de.baupilot.data.MockRepository
import de.baupilot.data.RepositoryException
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** D11: a load failure is logged and leaves no data; a good fixture loads. Robolectric supplies `Log`. */
@RunWith(RobolectricTestRunner::class)
class BaupilotRootLoadTest {
    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()

    @Test
    fun missingFixtureYieldsNull() = runTest {
        assertNull(loadOrNull { throw RepositoryException.FixtureMissing("baupilot.json") })
    }

    @Test
    fun undecodableFixtureYieldsNull() = runTest {
        assertNull(loadOrNull { MockRepository("{}") })
    }

    @Test
    fun goodFixtureYieldsData() = runTest {
        assertNotNull(loadOrNull { MockRepository(fixture) })
    }
}
