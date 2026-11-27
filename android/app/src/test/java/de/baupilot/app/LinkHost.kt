package de.baupilot.app

import androidx.activity.ComponentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import de.baupilot.snapshot.TestAppData

/**
 * Test-only host for the link tests (plan D5): starts the real app content at any navigation state, so a link test begins
 * on the source screen. The `BackHandler` is copied from `BaupilotRoot` (without it a system back pops nothing).
 * [start] may be called again within one test: it restarts the content at the new state.
 */
class LinkHost(private val actions: AppActions = AppActions.None) {
    val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>()

    private val data by lazy { TestAppData.load() }
    private var generation = 0
    private val launch = mutableStateOf<Pair<Int, AppNavigationState>?>(null)

    fun start(start: AppNavigationState) {
        val first = launch.value == null
        launch.value = ++generation to start
        if (first) {
            rule.setContent {
                val current = launch.value ?: return@setContent
                key(current.first) {
                    var state by remember { mutableStateOf(current.second) }
                    BackHandler(enabled = state.canPop) { state = state.pop() }
                    BaupilotAppContent(state, onStateChange = { state = it }, data = data, actions = actions)
                }
            }
        }
        rule.waitForIdle()
    }
}
