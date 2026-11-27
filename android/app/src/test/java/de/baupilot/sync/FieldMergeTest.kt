package de.baupilot.sync

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.random.Random
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FieldMergeTest {
    private val d1 = "device-1"
    private val d2 = "device-2"

    private fun record(
        fields: Map<String, JsonElement> = emptyMap(),
        stamps: Map<String, FieldStamp> = emptyMap(),
        deleted: Boolean = false,
        id: String = "t1",
        projectId: String = "p1",
        sortKey: Long = 1,
    ) = SyncRecord(SyncTable.Trades, id, projectId, sortKey, JsonObject(fields), stamps, deleted)

    private fun str(s: String) = JsonPrimitive(s)

    @Test
    fun newerStampWinsPerField() {
        val base = record(
            mapOf("name" to str("Rohbau"), "status" to str("open")),
            mapOf("name" to FieldStamp(1, d1), "status" to FieldStamp(1, d1)),
        )
        val a = FieldMerge.touch(base, mapOf("name" to str("Rohbau neu")), FieldStamp(5, d1))
        val b = FieldMerge.touch(base, mapOf("status" to str("done")), FieldStamp(6, d2))
        val merged = FieldMerge.merge(a, b)
        assertEquals(str("Rohbau neu"), merged.fields["name"])
        assertEquals(str("done"), merged.fields["status"])
        assertEquals(FieldStamp(5, d1), merged.stamps["name"])
        assertEquals(FieldStamp(6, d2), merged.stamps["status"])
        assertEquals(merged, FieldMerge.merge(b, a))
    }

    @Test
    fun sameFieldLaterWins() {
        val base = record(mapOf("name" to str("x")), mapOf("name" to FieldStamp(1, d1)))
        val early = FieldMerge.touch(base, mapOf("name" to str("early")), FieldStamp(5, d1))
        val late = FieldMerge.touch(base, mapOf("name" to str("late")), FieldStamp(9, d2))
        assertEquals(str("late"), FieldMerge.merge(early, late).fields["name"])
        assertEquals(str("late"), FieldMerge.merge(late, early).fields["name"])
        // the device id breaks an equal `at`
        val sameAt = FieldMerge.touch(base, mapOf("name" to str("second")), FieldStamp(5, d2))
        assertEquals(str("second"), FieldMerge.merge(early, sameAt).fields["name"])
    }

    @Test
    fun equalStampsResolveDeterministically() {
        val stamp = FieldStamp(7, d1)
        val a = record(mapOf("name" to str("alpha")), mapOf("name" to stamp))
        val b = record(mapOf("name" to str("beta")), mapOf("name" to stamp))
        assertEquals(str("beta"), FieldMerge.merge(a, b).fields["name"])
        assertEquals(str("beta"), FieldMerge.merge(b, a).fields["name"])
        // a value beats an absent key on an equal stamp
        val absent = record(emptyMap(), mapOf("name" to stamp))
        assertEquals(str("alpha"), FieldMerge.merge(a, absent).fields["name"])
        assertEquals(str("alpha"), FieldMerge.merge(absent, a).fields["name"])
    }

    @Test
    fun absentValueWithWinningStampRemovesKey() {
        val a = record(mapOf("note" to str("x")), mapOf("note" to FieldStamp(1, d1)))
        val b = record(emptyMap(), mapOf("note" to FieldStamp(2, d2)))
        val merged = FieldMerge.merge(a, b)
        assertFalse(merged.fields.containsKey("note"))
        assertEquals(FieldStamp(2, d2), merged.stamps["note"])
    }

    @Test
    fun mergeRejectsDifferentTableOrId() {
        val a = record()
        try {
            FieldMerge.merge(a, a.copy(id = "other"))
            fail("expected IllegalArgumentException for a different id")
        } catch (_: IllegalArgumentException) {
        }
        try {
            FieldMerge.merge(a, a.copy(table = SyncTable.Offers))
            fail("expected IllegalArgumentException for a different table")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun projectIdAndSortKeyTakeNonBlankAndSmallestNonZero() {
        val a = record(projectId = "", sortKey = 0)
        val b = record(projectId = "p9", sortKey = 7)
        val c = record(projectId = "p9", sortKey = 3)
        assertEquals("p9", FieldMerge.merge(a, b).projectId)
        assertEquals(7L, FieldMerge.merge(a, b).sortKey)
        assertEquals(3L, FieldMerge.merge(b, c).sortKey)
        assertEquals(0L, FieldMerge.merge(a, a).sortKey)
    }

    @Test
    fun touchChangesOnlyTheNamedKeys() {
        val base = record(
            mapOf("name" to str("x"), "status" to str("open")),
            mapOf("name" to FieldStamp(1, d1), "status" to FieldStamp(1, d1)),
        )
        val touched = FieldMerge.touch(base, mapOf("name" to str("y")), FieldStamp(4, d2))
        assertEquals(str("y"), touched.fields["name"])
        assertEquals(FieldStamp(4, d2), touched.stamps["name"])
        assertEquals(base.fields["status"], touched.fields["status"])
        assertEquals(FieldStamp(1, d1), touched.stamps["status"])
        assertFalse(touched.stamps.containsKey("deleted"))
    }

    @Test
    fun deleteIsAFieldTombstone() {
        val base = record(mapOf("name" to str("x")), mapOf("name" to FieldStamp(1, d1)))
        val deleted = FieldMerge.touch(base, emptyMap(), FieldStamp(5, d1), deleted = true)
        assertTrue(deleted.deleted)
        val undeleted = FieldMerge.touch(deleted, emptyMap(), FieldStamp(9, d2), deleted = false)
        assertFalse(FieldMerge.merge(deleted, undeleted).deleted)
        assertFalse(FieldMerge.merge(undeleted, deleted).deleted)
        // an earlier undelete loses against a later delete
        val laterDelete = FieldMerge.touch(undeleted, emptyMap(), FieldStamp(12, d1), deleted = true)
        assertTrue(FieldMerge.merge(undeleted, laterDelete).deleted)
        assertEquals(FieldStamp(12, d1), FieldMerge.merge(undeleted, laterDelete).stamps["deleted"])
    }

    @Test
    fun mergeIsCommutativeIdempotentAssociative() {
        val rnd = Random(20260611)
        repeat(500) { case ->
            val a = randomRecord(rnd)
            val b = randomRecord(rnd)
            val c = randomRecord(rnd)
            assertEquals("commutative #$case", FieldMerge.merge(a, b), FieldMerge.merge(b, a))
            assertEquals(
                "associative #$case",
                FieldMerge.merge(FieldMerge.merge(a, b), c),
                FieldMerge.merge(a, FieldMerge.merge(b, c)),
            )
            val ab = FieldMerge.merge(a, b)
            assertEquals("idempotent #$case", ab, FieldMerge.merge(ab, ab))
            assertEquals("idempotent with input #$case", ab, FieldMerge.merge(ab, a))
        }
    }

    // Small domains force equal stamps, equal keys and absent values.
    private fun randomRecord(rnd: Random): SyncRecord {
        val keys = listOf("name", "status", "note", "detail")
        val fields = linkedMapOf<String, JsonElement>()
        val stamps = linkedMapOf<String, FieldStamp>()
        for (key in keys) {
            if (rnd.nextInt(4) != 0) stamps[key] = FieldStamp(1L + rnd.nextInt(3), listOf(d1, d2)[rnd.nextInt(2)])
            when (rnd.nextInt(4)) {
                0 -> {}
                1 -> fields[key] = str("v1")
                2 -> fields[key] = str("v2")
                else -> fields[key] = JsonObject(mapOf("k" to JsonPrimitive(rnd.nextInt(2)), "a" to JsonPrimitive("z")))
            }
        }
        if (rnd.nextInt(4) != 0) stamps["deleted"] = FieldStamp(1L + rnd.nextInt(3), listOf(d1, d2)[rnd.nextInt(2)])
        return SyncRecord(
            table = SyncTable.Trades,
            id = "t1",
            projectId = listOf("", "p1", "p2")[rnd.nextInt(3)],
            sortKey = rnd.nextInt(3).toLong(),
            fields = JsonObject(fields),
            stamps = stamps,
            deleted = rnd.nextBoolean(),
        )
    }

    @Test
    fun stampClockStrictlyIncreasesUnderFrozenClock() {
        val frozen = Clock.fixed(Instant.ofEpochMilli(1_000), ZoneOffset.UTC)
        val clock = StampClock(frozen, d1, maxSeen = { 0L })
        val first = clock.next()
        assertEquals(FieldStamp(1_000, d1), first)
        var previous = first
        repeat(100) {
            val next = clock.next()
            assertTrue(next.at > previous.at)
            assertEquals(d1, next.by)
            previous = next
        }
    }

    @Test
    fun stampClockJumpsPastMaxSeen() {
        val frozen = Clock.fixed(Instant.ofEpochMilli(1_000), ZoneOffset.UTC)
        var seen = 0L
        val clock = StampClock(frozen, d1, maxSeen = { seen })
        assertEquals(1_000L, clock.next().at)
        seen = 50_000
        assertEquals(50_001L, clock.next().at)
        assertEquals(50_002L, clock.next().at)
        seen = 10
        assertEquals(50_003L, clock.next().at)
    }
}
