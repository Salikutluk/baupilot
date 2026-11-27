package de.baupilot.backend

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PushRegistrarTest {
    private class FakeApi : PushTargetApi {
        val calls = mutableListOf<String>()
        override suspend fun create(targetId: String, token: String, providerId: String) {
            calls += "create:$targetId:$token:$providerId"
        }
        override suspend fun update(targetId: String, token: String) { calls += "update:$targetId:$token" }
        override suspend fun delete(targetId: String) { calls += "delete:$targetId" }
    }

    private lateinit var prefs: SharedPreferences
    private val api = FakeApi()

    @Before
    fun setUp() {
        prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("push-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    private fun registrar(token: String?, scope: CoroutineScope) =
        AppwritePushRegistrar(api, prefs, { token }, scope)

    @Test
    fun registerCreatesTheTargetOnce() = runTest {
        val r = registrar("tok1", this)
        r.register("u1")
        r.register("u1")
        assertEquals(1, api.calls.size)
        assertTrue(api.calls[0].startsWith("create:"))
        assertTrue(api.calls[0].endsWith(":tok1:fcm"))
    }

    @Test
    fun newTokenUpdatesTheExistingTarget() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val r = registrar("tok1", scope)
        r.register("u1")
        val id = api.calls[0].split(":")[1]
        r.onNewToken("tok2")
        assertEquals("update:$id:tok2", api.calls[1])
        r.onNewToken("tok2")
        assertEquals(2, api.calls.size)
    }

    @Test
    fun tokenBeforeRegisterIsKeptForRegister() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val r = registrar(null, scope)
        r.onNewToken("tok9")
        assertEquals(emptyList<String>(), api.calls)
        r.register("u1")
        assertTrue(api.calls.single().endsWith(":tok9:fcm"))
    }

    @Test
    fun noTokenMeansNoTarget() = runTest {
        registrar(null, this).register("u1")
        assertEquals(emptyList<String>(), api.calls)
    }

    @Test
    fun unregisterDeletesAndForgets() = runTest {
        val r = registrar("tok1", this)
        r.register("u1")
        val id = api.calls[0].split(":")[1]
        r.unregister()
        assertEquals("delete:$id", api.calls[1])
        r.unregister()
        assertEquals(2, api.calls.size)
        r.register("u1")
        assertTrue(api.calls[2].startsWith("create:"))
    }

    @Test
    fun noPushDoesNothing() = runTest {
        NoPush.register("u")
        NoPush.onNewToken("t")
        NoPush.unregister()
    }
}
