package de.baupilot.app

import android.net.Uri
import android.os.Looper
import de.baupilot.BaupilotApplication
import de.baupilot.capture.ImportSource
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/** M8, M7: a second tap while the first is running does nothing; the work outlives the composition's scope. */
@RunWith(RobolectricTestRunner::class)
class RootActionsGuardTest {
    private class Env : RootEnvironment {
        var launches = 0
        var documents = 0
        val toasts = mutableListOf<String>()

        override fun toast(text: String) {
            toasts += text
        }

        override fun uriFor(file: File): Uri = Uri.fromFile(file)
        override fun launchCamera(target: Uri) {
            launches++
        }

        override fun launchDocument() {
            documents++
        }

        override fun source(uri: Uri): ImportSource = object : ImportSource {
            override val displayName: String? = null
            override fun open(): InputStream = ByteArrayInputStream(ByteArray(0))
        }

        override fun confirm() = Unit
    }

    private val services get() = (RuntimeEnvironment.getApplication() as BaupilotApplication).services
    private val env = Env()
    private val session = CaptureSession()
    private val uiScope = CoroutineScope(SupervisorJob())
    private val route = AppRoute.InspectionStep("shell", "W1", 2)

    @After
    fun tearDown() {
        uiScope.cancel()
    }

    private fun idleUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5_000
        while (!condition() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
    }

    @Test
    fun doubleTapOnThePhotoControlLaunchesOneCamera() {
        runBlocking { services.repository.ensureSeeded() }
        val actions = RootActions(services, { null }, session, uiScope, env)
        actions.capturePhoto(route, null)
        actions.capturePhoto(route, null)
        idleUntil { env.launches >= 1 }
        actions.capturePhoto(route, null)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, env.launches)
        val pending = session.pending
        assertNotNull(pending)

        actions.onPhotoResult(false)
        assertNull(session.pending)
        actions.capturePhoto(route, null)
        idleUntil { env.launches >= 2 }
        assertEquals(2, env.launches)
    }

    @Test
    fun importWhileAPickerIsOpenIsIgnored() {
        val actions = RootActions(services, { null }, session, uiScope, env)
        actions.importDocument(null, "plan")
        actions.importDocument(null, "plan")
        assertEquals(1, env.documents)
        actions.onDocumentResult(null)
        actions.importDocument(null, "plan")
        assertEquals(2, env.documents)
    }

    @Test
    fun signOutStillClearsTheSessionWhenTheCompositionIsGone() {
        runBlocking { services.auth.logIn("", "") }
        val actions = RootActions(services, { null }, session, uiScope, env)
        var done = 0
        actions.signOut { done++ }
        uiScope.cancel()
        idleUntil { services.auth.cachedUserId == null }
        assertNull(services.auth.cachedUserId)
    }
}
