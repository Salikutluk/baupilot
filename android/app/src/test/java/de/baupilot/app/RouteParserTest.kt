package de.baupilot.app

import de.baupilot.data.RepositoryException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class RouteParserTest {
    private fun assertDecoding(field: String, block: () -> Unit) {
        try {
            block()
            fail("expected Decoding($field)")
        } catch (e: RepositoryException.Decoding) {
            assertEquals(field, e.detail)
        }
    }

    @Test
    fun parsesTabs() {
        mapOf(
            "home" to AppTab.Home,
            "schedule" to AppTab.Schedule,
            "inspections" to AppTab.Inspections,
            "documents" to AppTab.Documents,
            "profile" to AppTab.Profile,
        ).forEach { (raw, tab) ->
            assertEquals(NavigationTarget.Tab(tab), RouteParser.target(raw, "f"))
        }
    }

    @Test
    fun defectsMapsToInspectionsTab() {
        assertEquals(NavigationTarget.Tab(AppTab.Inspections), RouteParser.target("defects", "f"))
    }

    @Test
    fun parsesPushRoutes() {
        assertEquals(NavigationTarget.Push(AppRoute.Notifications), RouteParser.target("notifications", "f"))
        assertEquals(NavigationTarget.Push(AppRoute.Trade("screed")), RouteParser.target("trade/screed", "f"))
        assertEquals(NavigationTarget.Push(AppRoute.Trade("a_b-1")), RouteParser.target("trade/a_b-1", "f"))
        assertEquals(
            NavigationTarget.Push(AppRoute.Compare("tiles")),
            RouteParser.target("trade/tiles/compare", "f"),
        )
        assertEquals(NavigationTarget.Push(AppRoute.Document("doc-1")), RouteParser.target("document/doc-1", "f"))
    }

    @Test
    fun nullIsNull() {
        assertNull(RouteParser.target(null, "f"))
        assertNull(RouteParser.route(null, "f"))
    }

    @Test
    fun routeReturnsPushRoutes() {
        assertEquals(AppRoute.Trade("screed"), RouteParser.route("trade/screed", "f"))
        assertEquals(AppRoute.Notifications, RouteParser.route("notifications", "f"))
    }

    @Test
    fun malformedTargetsThrowDecoding() {
        listOf(
            "", "elsewhere/1", "trade/", "trade", "trade//compare", "trade/-x", "trade/a b",
            "trade/a/compare/b", "trade/a/other", "document/", "document", "document/a/b",
            "Home", "notifications/1", "/trade/a",
        ).forEach { raw -> assertDecoding("schedule.asks.target") { RouteParser.target(raw, "schedule.asks.target") } }
    }

    @Test
    fun routeRejectsTabTargets() {
        assertDecoding("field") { RouteParser.route("home", "field") }
        assertDecoding("field") { RouteParser.route("defects", "field") }
        assertDecoding("field") { RouteParser.route("bogus", "field") }
    }
}
