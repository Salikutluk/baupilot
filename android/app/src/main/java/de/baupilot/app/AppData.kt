package de.baupilot.app

import de.baupilot.data.BackendRepository
import de.baupilot.data.HomeScreenText
import de.baupilot.data.LoginScreenText
import de.baupilot.data.ProfilScreenText
import de.baupilot.data.RepositoryException
import de.baupilot.data.TabText
import de.baupilot.data.ZeitplanScreenText
import de.baupilot.data.screenText
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.schedule.ScheduleLayout
import de.baupilot.screens.ScreenCopy

data class LoginModel(
    val chips: List<String>,
    val title: String,
    val intro: String,
    val emailLabel: String,
    val emailPlaceholder: String,
    val passwordLabel: String,
    val forgot: String,
    val submit: String,
    val social: String,
    val create: String,
)

data class HomeTradeRow(
    val initial: String,
    val name: String,
    val `when`: String,
    val statusLabel: String,
    val tradeId: String,
    val colors: StatusChipColors,
)

data class HomeDocTile(val label: String, val count: String, val icon: BaupilotIcon)

data class HomeModel(
    val projectLabel: String,
    val notificationsAria: String,
    val badge: String,
    val phaseLabel: String,
    val phaseTitle: String,
    val progress: Float,
    val moveInLabel: String,
    val trades: List<HomeTradeRow>,
    val docs: List<HomeDocTile>,
    val tabs: List<TabText>,
)

data class ScheduleAskRow(val name: String, val sub: String, val target: AppRoute?)

data class ScheduleModel(
    val moveInLabel: String,
    val ariaLabel: String,
    val asks: List<ScheduleAskRow>,
    val layout: ScheduleLayout,
    val tabs: List<TabText>,
)

data class ProfileRow(val label: String, val sub: String?, val isToggle: Boolean, val checked: Boolean?)

data class ProfileGroup(val title: String, val rows: List<ProfileRow>)

data class ProfileModel(
    val initials: String,
    val name: String,
    val email: String,
    val groups: List<ProfileGroup>,
    val tabs: List<TabText>,
)

/** Every screen model, built once from the repository (D11). */
data class AppData(
    val login: LoginModel,
    val home: HomeModel,
    val schedule: ScheduleModel,
    val profile: ProfileModel,
    val tradeNames: Map<String, String>,
) {
    companion object {
        /** Content width of the Zeitplan chart on the 390 dp screen (AndZeitplan.dc.html), fixed, not measured. */
        private const val CHART_WIDTH = 358f
        private const val TRADE_TARGET_PREFIX = "trade/"

        /** Document tile icons by position: document, contract, receipt, map (AndHome.dc.html:106-109). */
        private val DOC_ICONS = listOf(BaupilotIcon.Document, BaupilotIcon.Contract, BaupilotIcon.Receipt, BaupilotIcon.Map)

        suspend fun load(repository: BackendRepository): AppData {
            val project = repository.project()
            val trades = repository.trades()
            val schedule = repository.schedule()
            val loginText = repository.screenText("Login", LoginScreenText.serializer())
            val homeText = repository.screenText("Home", HomeScreenText.serializer())
            val zeitplanText = repository.screenText("Zeitplan", ZeitplanScreenText.serializer())
            val profilText = repository.screenText("Profil", ProfilScreenText.serializer())

            val phaseTitle = trades.firstOrNull { it.id == project.phase.tradeId }?.name
                ?: throw RepositoryException.Decoding("project.phase.tradeId")

            val login = LoginModel(
                chips = loginText.chips,
                title = loginText.title,
                intro = loginText.intro,
                emailLabel = loginText.emailLabel,
                emailPlaceholder = loginText.emailPlaceholder,
                passwordLabel = loginText.passwordLabel,
                forgot = loginText.forgot,
                submit = loginText.submit,
                social = loginText.social,
                create = loginText.create,
            )
            val home = HomeModel(
                projectLabel = homeText.projectLabel,
                notificationsAria = homeText.notificationsAria,
                badge = homeText.badge,
                phaseLabel = homeText.phaseLabel,
                phaseTitle = phaseTitle,
                progress = project.phase.progressPercent / 100f,
                moveInLabel = homeText.moveInLabel,
                trades = homeText.trades.map { row ->
                    HomeTradeRow(
                        initial = row.initial,
                        name = row.name,
                        `when` = row.`when`,
                        statusLabel = row.statusLabel,
                        tradeId = row.tradeId,
                        colors = ScreenCopy.Home.tone(row.tradeId)
                            ?: throw RepositoryException.Decoding("screens.Home.android.trades.tradeId"),
                    )
                },
                docs = homeText.docs.mapIndexed { index, doc ->
                    HomeDocTile(
                        doc.label,
                        doc.count,
                        DOC_ICONS.getOrNull(index) ?: throw RepositoryException.Decoding("screens.Home.android.docs"),
                    )
                },
                tabs = homeText.tabs,
            )
            val scheduleModel = ScheduleModel(
                moveInLabel = zeitplanText.moveInLabel,
                ariaLabel = zeitplanText.ariaLabel,
                asks = schedule.asks.map { ask -> ScheduleAskRow(ask.name, ask.sub, parseTarget(ask.target)) },
                layout = ScheduleLayout.make(trades, schedule, CHART_WIDTH),
                tabs = zeitplanText.tabs,
            )
            val profile = ProfileModel(
                initials = project.owner.initials,
                name = project.owner.name,
                email = project.owner.email,
                groups = profilText.groups.map { group ->
                    ProfileGroup(
                        group.title,
                        group.rows.map { ProfileRow(it.label, it.sub, it.isToggle, it.checked) },
                    )
                },
                tabs = profilText.tabs,
            )
            return AppData(login, home, scheduleModel, profile, trades.associate { it.id to it.name })
        }

        private fun parseTarget(target: String?): AppRoute? = when {
            target == null -> null
            target.startsWith(TRADE_TARGET_PREFIX) && target.length > TRADE_TARGET_PREFIX.length ->
                AppRoute.Trade(target.removePrefix(TRADE_TARGET_PREFIX))
            else -> throw RepositoryException.Decoding("schedule.asks.target")
        }
    }
}
