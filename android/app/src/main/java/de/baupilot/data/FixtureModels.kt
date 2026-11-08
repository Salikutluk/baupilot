package de.baupilot.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Fixture(
    val meta: Meta,
    val project: Project,
    val trades: List<Trade>,
    val schedule: Schedule,
    val offers: List<Offer>,
    val comparisons: List<Comparison>,
    val documents: Documents,
    val inspections: JsonElement,
    val measurements: JsonElement,
    val defects: List<Defect>,
    val resolvedDefects: ResolvedDefects,
    val notifications: List<NotificationGroup>,
    val screens: JsonElement,
)

@Serializable
data class Meta(val schemaVersion: Int, val source: String, val now: String)

@Serializable
data class Owner(val name: String, val initials: String, val email: String, val role: String)

@Serializable
data class Phase(val current: Int, val total: Int, val tradeId: String, val progressPercent: Int)

@Serializable
data class Project(
    val id: String,
    val buildingType: String,
    val address: String,
    val constructionStart: String,
    val moveIn: String,
    val phase: Phase,
    val unreadNotifications: Int,
    val owner: Owner,
)

@Serializable
data class TradeWindow(val startMonth: Double, val endMonth: Double)

@Serializable
data class Fact(val label: String, val value: String)

@Serializable
data class TradeDetail(
    val phaseLabel: String,
    val facts: List<Fact>,
    val offersSummary: PlatformText,
)

@Serializable
data class Trade(
    val id: String,
    val name: String,
    val phase: Int,
    val window: TradeWindow,
    val scheduleState: String,
    val askNow: Boolean,
    val weekLabel: String? = null,
    val statusLabel: String? = null,
    val detail: TradeDetail? = null,
)

@Serializable
data class AxisTick(val month: Int, val label: String)

@Serializable
data class ScheduleAsk(
    val tradeIds: List<String>,
    val name: String,
    val sub: String,
    val target: String?,
)

@Serializable
data class Schedule(
    val originLabel: String,
    val spanMonths: Int,
    val todayMonth: Double,
    val axis: List<AxisTick>,
    val asks: List<ScheduleAsk>,
)

@Serializable
data class Offer(
    val id: String,
    val tradeId: String,
    val firm: String,
    val price: String,
    val best: Boolean,
    val pending: Boolean,
    val meta: PlatformText,
    val documentId: String? = null,
)

@Serializable
data class ComparisonRow(val label: String, val a: String, val b: String, val strong: Boolean)

@Serializable
data class Comparison(
    val tradeId: String,
    val offerIds: List<String>,
    val columns: List<String>,
    val rows: List<ComparisonRow>,
    val summary: PlatformText,
    val hint: PlatformText,
    val pending: PlatformText,
)

@Serializable
data class DocumentCounts(
    val all: Int,
    val offer: Int,
    val contract: Int,
    val invoice: Int,
    val plan: Int,
    val trades: Int,
)

@Serializable
data class DocumentCategory(val kind: String, val label: String, val count: String)

@Serializable
data class ViewerIssuer(val name: String, val address: String)

@Serializable
data class ViewerLine(val pos: String, val text: String, val sum: String)

@Serializable
data class DocumentViewer(
    val subtitle: PlatformText,
    val pageLabel: String,
    val pageFooter: PlatformText,
    val issuer: ViewerIssuer,
    val number: String,
    val reference: String,
    val total: String,
    val lines: List<ViewerLine>,
    val facts: List<Fact>,
)

@Serializable
data class DocumentItem(
    val id: String,
    val kind: String,
    val tradeId: String? = null,
    val title: String,
    val meta: PlatformText,
    val amount: String? = null,
    val due: String? = null,
    val statusLabel: String? = null,
    val viewer: DocumentViewer? = null,
)

@Serializable
data class Documents(
    val counts: DocumentCounts,
    val categories: List<DocumentCategory>,
    val items: List<DocumentItem>,
)

@Serializable
data class Defect(
    val id: String,
    val tradeId: String,
    val trade: PlatformText,
    val title: String,
    val deadline: String,
    val deadlineLabel: String,
    val dueLabel: String,
    val urgency: String,
    val meta: String,
    val photos: Int? = null,
    val severity: String? = null,
    val description: String? = null,
    val elementId: String? = null,
    val responsible: String? = null,
    val notifyByEmail: Boolean? = null,
)

@Serializable
data class ResolvedDefects(val count: String)

@Serializable
data class NotificationItem(
    val id: String,
    val kind: String,
    val title: String,
    val text: String,
    val `when`: String,
    val unread: Boolean,
    val target: String?,
)

@Serializable
data class NotificationGroup(val title: String, val items: List<NotificationItem>)
