package de.baupilot.backend

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import de.baupilot.sync.RemoteErrorKind
import de.baupilot.sync.RemoteException
import de.baupilot.sync.SyncBucket
import de.baupilot.sync.SyncTable
import io.appwrite.Client
import java.io.File
import java.net.InetSocketAddress
import java.util.Collections
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/** The real SDK against a local HTTP server: what goes over the wire is what the gateway promises (M1, M2, C2, M3). */
@RunWith(RobolectricTestRunner::class)
class AppwriteGatewayTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private class Seen(val method: String, val path: String, val range: String?, val body: String)

    private val seen: MutableList<Seen> = Collections.synchronizedList(ArrayList())
    private lateinit var server: HttpServer
    private lateinit var gateway: AppwriteGateway

    /** Answer for GET/DELETE requests: status and Appwrite error type. */
    private var errorStatus = 404
    private var errorType = "row_not_found"

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { ex -> handle(ex) }
        server.start()
        val client = Client(RuntimeEnvironment.getApplication())
            .setEndpoint("http://127.0.0.1:${server.address.port}/v1")
            .setProject("proj")
        gateway = AppwriteGateway(client)
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    private fun handle(ex: HttpExchange) {
        val body = String(ex.requestBody.readBytes(), Charsets.ISO_8859_1)
        val range = ex.requestHeaders.getFirst("Content-Range")
        seen += Seen(ex.requestMethod, ex.requestURI.path, range, body)
        val response: String
        val status: Int
        if (ex.requestMethod == "POST") {
            val total = range?.substringAfter('/')?.toLong() ?: body.length.toLong()
            val end = range?.substringAfter(' ')?.substringBefore('/')?.substringAfter('-')?.toLong() ?: (total - 1)
            val chunks = (total + CHUNK - 1) / CHUNK
            val uploaded = if (end + 1 >= total) chunks else (end + 1) / CHUNK
            status = 201
            response = """{"${'$'}id":"f1","bucketId":"photos","${'$'}createdAt":"t","${'$'}updatedAt":"t","${'$'}permissions":[],
                |"name":"n","signature":"s","mimeType":"image/jpeg","sizeOriginal":$total,"sizeActual":$total,"chunksTotal":$chunks,"chunksUploaded":$uploaded,
                |"encryption":false,"compression":"none"}"""
                .trimMargin().replace("\n", "")
        } else {
            status = errorStatus
            response = """{"message":"m","code":$errorStatus,"type":"$errorType","version":"1"}"""
        }
        val bytes = response.toByteArray()
        ex.responseHeaders.add("Content-Type", "application/json")
        ex.sendResponseHeaders(status, bytes.size.toLong())
        ex.responseBody.use { it.write(bytes) }
    }

    /**
     * The test thread is Robolectric's main thread: a `runBlocking` there would starve every callback the SDK posts to the
     * main looper. The call runs on IO while this thread keeps the looper turning.
     */
    private fun runGateway(block: suspend () -> Unit) {
        val job = CoroutineScope(Dispatchers.IO).async { block() }
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (!job.isCompleted && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        assertTrue("gateway call did not finish", job.isCompleted)
        runBlocking { job.await() }
    }

    private fun file(size: Int): File = tmp.newFile("ph_x.jpg").also { it.writeBytes(ByteArray(size) { 7 }) }

    @Test
    fun smallUploadCarriesThePlannedName() = runGateway {
        gateway.uploadFile(SyncBucket.Photos, "ph_1", "musterweg-12/photos/ph_1.jpg", file(1000), "image/jpeg")
        assertEquals(listOf("POST"), seen.map { it.method })
        assertTrue(seen[0].body, seen[0].body.contains("filename=\"musterweg-12/photos/ph_1.jpg\""))
    }

    @Test
    fun uploadOfFiveMiBOrMoreSkipsTheLookUpThatReturns404() = runGateway {
        gateway.uploadFile(SyncBucket.Pdfs, "dc_1", "musterweg-12/documents/dc_1.pdf", file(CHUNK.toInt() + 100), "application/pdf")
        assertEquals(listOf("POST", "POST"), seen.map { it.method })
        assertTrue(seen[0].body.contains("filename=\"musterweg-12/documents/dc_1.pdf\""))
        assertTrue(seen.all { it.path == "/v1/storage/buckets/pdfs/files" })
    }

    @Test
    fun onlyRowNotFoundMeansTheRowIsMissing() = runGateway {
        assertNull(gateway.getRow(SyncTable.Projects, "p1"))
        errorType = "table_not_found"
        val failure = runCatching { gateway.getRow(SyncTable.Projects, "p1") }.exceptionOrNull() as RemoteException
        assertEquals(RemoteErrorKind.Transient, failure.kind)
    }

    @Test
    fun deleteOfAnotherNotFoundIsNotSuccess() = runGateway {
        errorType = "database_not_found"
        val failure = runCatching { gateway.deleteRow(SyncTable.Projects, "p1") }.exceptionOrNull() as RemoteException
        assertEquals(RemoteErrorKind.Transient, failure.kind)
    }

    @Test
    fun missingPermissionIsForbiddenNotAuth() = runGateway {
        errorStatus = 401
        errorType = "user_unauthorized"
        val failure = runCatching { gateway.getRow(SyncTable.Projects, "p1") }.exceptionOrNull() as RemoteException
        assertEquals(RemoteErrorKind.Forbidden, failure.kind)
    }

    @Test
    fun aGuestSessionIsAnAuthFailureNotAMissingMembership() = runGateway {
        errorStatus = 401
        errorType = "general_unauthorized_scope"
        val failure = runCatching { gateway.hasAccess("p1", "u1") }.exceptionOrNull() as RemoteException
        assertEquals(RemoteErrorKind.Auth, failure.kind)
        assertEquals(listOf("/v1/account"), seen.map { it.path })
    }

    private companion object {
        const val CHUNK = 5_242_880L
        const val TIMEOUT_MS = 20_000L
    }
}
