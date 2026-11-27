package de.baupilot.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.baupilot.capture.StepStatus
import de.baupilot.inspection.MeasureDimension
import de.baupilot.inspection.SignatureStrokes
import de.baupilot.snapshot.ScreenCatalog
import de.baupilot.snapshot.TestAppData
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Records every call; the callbacks of `logIn`, `signOut` and `finishSignOff` are never invoked. */
class RecordingActions : AppActions {
    data class Step(val route: AppRoute, val part: String?, val status: StepStatus?, val values: JsonObject)
    data class Photo(val route: AppRoute, val part: String?)
    data class Defect(val draft: DefectFormValues, val below: AppRoute?)
    data class SignOff(val inspectionId: String, val strokes: SignatureStrokes)
    data class Import(val tradeId: String?, val kind: String?)

    val logIns = mutableListOf<Pair<String, String>>()
    var signOuts = 0
    val steps = mutableListOf<Step>()
    val photos = mutableListOf<Photo>()
    val defects = mutableListOf<Defect>()
    val signOffs = mutableListOf<SignOff>()
    val imports = mutableListOf<Import>()

    val calls: Int get() = logIns.size + signOuts + steps.size + photos.size + defects.size + signOffs.size + imports.size

    override fun logIn(email: String, password: String, onSuccess: () -> Unit) {
        logIns += email to password
    }

    override fun signOut(onDone: () -> Unit) {
        signOuts++
    }

    override fun recordStep(route: AppRoute, part: String?, status: StepStatus?, values: JsonObject) {
        steps += Step(route, part, status, values)
    }

    override fun capturePhoto(route: AppRoute, part: String?) {
        photos += Photo(route, part)
    }

    override fun saveDefect(draft: DefectFormValues, below: AppRoute?) {
        defects += Defect(draft, below)
    }

    override fun finishSignOff(inspectionId: String, strokes: SignatureStrokes, onSuccess: () -> Unit) {
        signOffs += SignOff(inspectionId, strokes)
    }

    override fun importDocument(tradeId: String?, kind: String?) {
        imports += Import(tradeId, kind)
    }
}

/**
 * One test per row of the plan's §5.10 action table: start on the screen through [LinkHost] with a [RecordingActions],
 * tap the tag and assert the recorded call with its arguments (D6). The window is tall so no tap needs a scroll.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h3000dp")
class M8ActionsUiTest {
    private val actions = RecordingActions()
    private val host = LinkHost(actions)
    private val data by lazy { TestAppData.load() }

    @get:Rule
    val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = host.rule

    private fun start(screen: String, current: String) {
        host.start(ScreenCatalog.state(screen))
        awaitTag(current)
    }

    private fun awaitTag(tag: String) {
        rule.waitUntil(timeoutMillis = 5_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun tap(tag: String) {
        awaitTag(tag)
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }

    private val noValues = JsonObject(emptyMap())

    @Test
    fun loginSubmitPassesTypedEmailAndPassword() {
        start("Login", "screen.login")
        val fields = rule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("bauherr@beispiel.de")
        fields[1].performTextInput("geheim123")
        tap("login.submit")
        assertEquals(listOf("bauherr@beispiel.de" to "geheim123"), actions.logIns)
        assertEquals(1, actions.calls)
    }

    @Test
    fun profileRowSixSignsOut() {
        start("Profil", "screen.profile")
        tap("ph.Profil.row.6")
        assertEquals(1, actions.signOuts)
        assertEquals(1, actions.calls)
    }

    @Test
    fun abmeldenIsRow6() {
        start("Profil", "screen.profile")
        for (tag in listOf("ph.Profil.card", "ph.Profil.row.0", "ph.Profil.row.1", "ph.Profil.row.5")) {
            tap(tag)
            assertEquals("$tag must not sign out", 0, actions.signOuts)
        }
        assertEquals(0, actions.calls)
        tap("ph.Profil.row.6")
        assertEquals(1, actions.signOuts)
    }

    private val stepRoute = AppRoute.InspectionStep("windows", "F8", 3)

    @Test
    fun stepOkRecordsPassed() {
        start("AbnPruefung", "screen.inspection.windows.F8.step.3")
        tap("ph.AbnPruefung.ok")
        assertEquals(listOf(RecordingActions.Step(stepRoute, null, StepStatus.OK, noValues)), actions.steps)
    }

    @Test
    fun stepDefectRecordsDefectAndStillOpensTheForm() {
        start("AbnPruefung", "screen.inspection.windows.F8.step.3")
        tap("step.defect")
        assertEquals(listOf(RecordingActions.Step(stepRoute, null, StepStatus.DEFECT, noValues)), actions.steps)
        awaitTag("screen.defect.new")
    }

    @Test
    fun stepPhotoAddCapturesForTheStep() {
        start("AbnPruefung", "screen.inspection.windows.F8.step.3")
        tap("step.photo.add")
        assertEquals(listOf(RecordingActions.Photo(stepRoute, null)), actions.photos)
        assertEquals(1, actions.calls)
    }

    private fun stepOk(screen: String, current: String, route: AppRoute) {
        start(screen, current)
        tap("step.ok")
        assertEquals(listOf(RecordingActions.Step(route, null, StepStatus.OK, noValues)), actions.steps)
    }

    @Test
    fun elRaumStepOk() =
        stepOk("ElRaum", "screen.inspection.electrical.room.kitchen", AppRoute.InspectionRoom("electrical", "kitchen"))

    @Test
    fun elVerteilerStepOk() =
        stepOk("ElVerteiler", "screen.inspection.electrical.panel", AppRoute.InspectionCheck("electrical", "panel"))

    @Test
    fun elRauchmelderStepOk() =
        stepOk("ElRauchmelder", "screen.inspection.electrical.smoke", AppRoute.InspectionCheck("electrical", "smoke"))

    @Test
    fun maRaumStepOk() =
        stepOk("MaRaum", "screen.inspection.painting.room.living", AppRoute.InspectionRoom("painting", "living"))

    @Test
    fun measureSaveRecordsSteppedValues() {
        val route = AppRoute.InspectionCheck("shell", "measure")
        val initial = data.measureChecks.getValue(route).initial
        start("RohbauMasse", "screen.inspection.shell.measure")
        tap("measure.height.increment")
        tap("measure.save")
        val expected = initial.increment(MeasureDimension.HEIGHT)
        assertEquals(
            listOf(
                RecordingActions.Step(
                    route, null, null,
                    JsonObject(mapOf("actualW" to JsonPrimitive(expected.actualW), "actualH" to JsonPrimitive(expected.actualH))),
                ),
            ),
            actions.steps,
        )
    }

    @Test
    fun measurePhotoCapturesForTheCheck() {
        start("RohbauMasse", "screen.inspection.shell.measure")
        tap("measure.photo")
        assertEquals(listOf(RecordingActions.Photo(AppRoute.InspectionCheck("shell", "measure"), null)), actions.photos)
    }

    @Test
    fun slopeSaveRecordsSteppedValues() {
        val route = AppRoute.InspectionCheckItem("plumbing", "slope", "A3")
        val initial = data.slopeChecks.getValue(route).initial
        start("SanGefaelle", "screen.inspection.plumbing.slope.A3")
        tap("slope.drop.increment")
        tap("slope.save")
        val expected = initial.incrementDrop()
        assertEquals(
            listOf(
                RecordingActions.Step(
                    route, null, null,
                    JsonObject(mapOf("lenDm" to JsonPrimitive(expected.lenDm), "dropMm" to JsonPrimitive(expected.dropMm))),
                ),
            ),
            actions.steps,
        )
    }

    @Test
    fun slopePhotoCapturesForTheCheck() {
        start("SanGefaelle", "screen.inspection.plumbing.slope.A3")
        tap("slope.photo")
        assertEquals(
            listOf(RecordingActions.Photo(AppRoute.InspectionCheckItem("plumbing", "slope", "A3"), null)),
            actions.photos,
        )
    }

    @Test
    fun pressurePhotoCapturesForTheCheck() {
        start("SanDruck", "screen.inspection.plumbing.pressure")
        tap("pressure.photo")
        assertEquals(listOf(RecordingActions.Photo(AppRoute.InspectionCheck("plumbing", "pressure"), null)), actions.photos)
    }

    @Test
    fun flatnessSaveRecordsThePointList() {
        val route = AppRoute.InspectionCheck("screed", "flatness")
        val initial = data.flatnessChecks.getValue(route).initial
        start("EsEbenheit", "screen.inspection.screed.flatness")
        tap("flatness.gap.increment")
        tap("flatness.save")
        val expected = initial.increment().save().points
        assertEquals(
            listOf(
                RecordingActions.Step(
                    route, null, null,
                    JsonObject(mapOf("points" to JsonArray(expected.map { JsonPrimitive(it) }))),
                ),
            ),
            actions.steps,
        )
    }

    @Test
    fun flatnessPhotoCapturesForTheCheck() {
        start("EsEbenheit", "screen.inspection.screed.flatness")
        tap("flatness.photo")
        assertEquals(listOf(RecordingActions.Photo(AppRoute.InspectionCheck("screed", "flatness"), null)), actions.photos)
    }

    @Test
    fun paintPhotoCapturesForTheRoom() {
        start("MaRaum", "screen.inspection.painting.room.living")
        tap("paint.photo")
        assertEquals(
            listOf(RecordingActions.Photo(AppRoute.InspectionRoom("painting", "living"), null)),
            actions.photos,
        )
    }

    @Test
    fun tilesNoneRecordsPassed() {
        start("FlBad", "screen.inspection.tiles.room.bath")
        tap("tiles.none")
        assertEquals(
            listOf(RecordingActions.Step(AppRoute.InspectionRoom("tiles", "bath"), null, StepStatus.OK, noValues)),
            actions.steps,
        )
    }

    @Test
    fun sanBadObjectCellsRecordOkAndDefectPerRow() {
        val route = AppRoute.InspectionRoom("plumbing", "bathUpper")
        start("SanBad", "screen.inspection.plumbing.room.bathUpper")
        tap("object.check.0.ok")
        tap("object.check.1.defect")
        assertEquals(
            listOf(
                RecordingActions.Step(route, "object/0", StepStatus.OK, noValues),
                RecordingActions.Step(route, "object/1", StepStatus.DEFECT, noValues),
            ),
            actions.steps,
        )
    }

    @Test
    fun defectPhotoAddCapturesForTheDraft() {
        start("AbnMangel", "screen.defect.new")
        tap("defect.photo.add")
        assertEquals(listOf(RecordingActions.Photo(AppRoute.NewDefect, null)), actions.photos)
    }

    @Test
    fun defectSavePassesFormValuesAndTheRouteBelow() {
        val model = data.defectForm
        start("AbnMangel", "screen.defect.new")
        tap("defect.severity.2")
        tap("defect.deadline.0")
        tap("defect.save")
        assertEquals(
            listOf(
                RecordingActions.Defect(
                    DefectFormValues(model.tradeId, model.description, 2, 0, model.notifyInitial),
                    stepRoute,
                ),
            ),
            actions.defects,
        )
        awaitTag("screen.inspection.windows.sign")
    }

    @Test
    fun signFinishPassesTheDrawnStrokes() {
        start("AbnSignatur", "screen.inspection.windows.sign")
        rule.onNodeWithTag("sign.owner").performScrollTo().performTouchInput {
            swipe(Offset(width * 0.2f, height * 0.5f), Offset(width * 0.8f, height * 0.6f), durationMillis = 200)
        }
        rule.waitForIdle()
        tap("sign.finish")
        val call = actions.signOffs.single()
        assertEquals("windows", call.inspectionId)
        assertTrue("the drawn stroke reaches the action", call.strokes.strokes.isNotEmpty())
    }

    @Test
    fun documentsAddPassesNullKindForAlle() {
        start("Dokumente", "screen.documents")
        tap("ph.Dokumente.add")
        assertEquals(listOf(RecordingActions.Import(null, "plan")), actions.imports)
    }

    @Test
    fun documentsAddPassesTheActiveFilterKind() {
        start("Dokumente", "screen.documents")
        val kind = data.documents.filters[1].kind!!.key
        tap("documents.filter.$kind")
        tap("ph.Dokumente.add")
        assertEquals(listOf(RecordingActions.Import(null, kind)), actions.imports)
    }

    @Test
    fun gewerkAttachImportsAnOfferForTheTrade() {
        start("Gewerk", "screen.trade.screed")
        tap("ph.Gewerk.add")
        assertEquals(listOf(RecordingActions.Import(data.trades.getValue("screed").tradeId, "offer")), actions.imports)
    }
}
