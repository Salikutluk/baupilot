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
    fun defectsIsDefectsTarget() {
        assertEquals(NavigationTarget.Defects, RouteParser.target("defects", "f"))
    }

    @Test
    fun parsesInspectionRoutes() {
        assertEquals(
            NavigationTarget.Push(AppRoute.Inspection("windows")),
            RouteParser.target("inspection/windows", "f"),
        )
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionStep("windows", "F8", 3)),
            RouteParser.target("inspection/windows/F8/step/3", "f"),
        )
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionStep("a", "b-1", 12)),
            RouteParser.target("inspection/a/b-1/step/12", "f"),
        )
        assertEquals(
            NavigationTarget.Push(AppRoute.SignOff("windows")),
            RouteParser.target("inspection/windows/sign", "f"),
        )
        assertEquals(NavigationTarget.Push(AppRoute.NewDefect), RouteParser.target("defect/new", "f"))
        assertEquals(AppRoute.NewDefect, RouteParser.route("defect/new", "f"))
        assertEquals(AppRoute.Inspection("roofing"), RouteParser.route("inspection/roofing", "f"))
    }

    @Test
    fun malformedInspectionRoutesThrowDecoding() {
        listOf(
            "inspection/", "inspection", "inspection/a/b/step", "inspection/a/b/sign", "inspection/a/b/c/d", "inspection/a/b/room",
            "inspection/a/b/-x", "inspection//b/c", "inspection/a//c", "inspection/-a/b/c", "inspection/a/room", "inspection/a/F8/step", "inspection/a/F8/step/0",
            "inspection/a/F8/step/03", "inspection/a/F8/step/x", "inspection/a/F8/steps/3", "inspection/a/F8/step/-1",
            "inspection/a/F8/step/3/x", "inspection/-a", "inspection/a/-F8/step/3", "defect/old", "defect/new/x",
            "defect/", "defect", "inspection/a/sign/x", "inspection//sign",
            "inspection/a/step", "inspection/a/room/", "inspection/a/room/x/y", "inspection/a/-x",
            "inspection/a/room/-x", "inspection//measure",
        ).forEach { raw -> assertDecoding("inspections.overview.rows.route") { RouteParser.target(raw, "inspections.overview.rows.route") } }
    }

    @Test
    fun parsesCheckAndRoomRoutes() {
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionCheck("shell", "measure")),
            RouteParser.target("inspection/shell/measure", "f"),
        )
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionCheck("windows", "F8-2")),
            RouteParser.target("inspection/windows/F8-2", "f"),
        )
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionRoom("electrical", "kitchen")),
            RouteParser.target("inspection/electrical/room/kitchen", "f"),
        )
        assertEquals(AppRoute.InspectionRoom("a", "r_1"), RouteParser.route("inspection/a/room/r_1", "f"))
        assertEquals(NavigationTarget.Push(AppRoute.SignOff("shell")), RouteParser.target("inspection/shell/sign", "f"))
    }

    @Test
    fun parsesCheckItemRoutes() {
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionCheckItem("plumbing", "slope", "A3")),
            RouteParser.target("inspection/plumbing/slope/A3", "f"),
        )
        assertEquals(
            AppRoute.InspectionCheckItem("a", "b", "c"),
            RouteParser.route("inspection/a/b/c", "f"),
        )
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionCheckItem("a", "F8-2", "x_1")),
            RouteParser.inspectionLink("inspection/a/F8-2/x_1", "f"),
        )
        // room keeps priority over the item rule
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionRoom("a", "kitchen")),
            RouteParser.target("inspection/a/room/kitchen", "f"),
        )
    }

    @Test
    fun inspectionLinkAcceptsDefectsAndPushRejectsTab() {
        assertEquals(NavigationTarget.Defects, RouteParser.inspectionLink("defects", "f"))
        assertEquals(
            NavigationTarget.Push(AppRoute.InspectionCheck("shell", "measure")),
            RouteParser.inspectionLink("inspection/shell/measure", "f"),
        )
        assertEquals(
            NavigationTarget.Push(AppRoute.Notifications),
            RouteParser.inspectionLink("notifications", "f"),
        )
        assertDecoding("field") { RouteParser.inspectionLink("home", "field") }
        assertDecoding("field") { RouteParser.inspectionLink("bogus", "field") }
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
    fun routeRejectsDefectsTarget() {
        assertDecoding("field") { RouteParser.route("defects", "field") }
    }

    @Test
    fun routeRejectsTabTargets() {
        assertDecoding("field") { RouteParser.route("home", "field") }
        assertDecoding("field") { RouteParser.route("defects", "field") }
        assertDecoding("field") { RouteParser.route("bogus", "field") }
    }
}
