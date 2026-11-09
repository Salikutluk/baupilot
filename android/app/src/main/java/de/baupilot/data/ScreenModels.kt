package de.baupilot.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/** One bottom-bar tab; `route` equals `AppTab.route`. */
@Serializable
data class TabText(val label: String, val route: String)

/** Mirror of `screens.Login.android`. */
@Serializable
data class LoginScreenText(
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

@Serializable
data class HomeTradeText(
    val initial: String,
    val name: String,
    val `when`: String,
    val statusLabel: String,
    val tradeId: String,
)

@Serializable
data class HomeDocText(val label: String, val count: String)

/** Mirror of `screens.Home.android`. */
@Serializable
data class HomeScreenText(
    val projectLabel: String,
    val notificationsAria: String,
    val badge: String,
    val phaseLabel: String,
    val moveInLabel: String,
    val trades: List<HomeTradeText>,
    val docs: List<HomeDocText>,
    val tabs: List<TabText>,
)

/** Mirror of `screens.Zeitplan.android`. */
@Serializable
data class ZeitplanScreenText(
    val moveInLabel: String,
    val ariaLabel: String,
    val tabs: List<TabText>,
)

@Serializable
data class ProfilRowText(
    val label: String,
    val sub: String? = null,
    val isToggle: Boolean,
    val checked: Boolean? = null,
)

@Serializable
data class ProfilGroupText(val title: String, val rows: List<ProfilRowText>)

/** Mirror of `screens.Profil.android`. */
@Serializable
data class ProfilScreenText(val groups: List<ProfilGroupText>, val tabs: List<TabText>)

@Serializable
data class SelectableTabText(val label: String, val selected: Boolean)

/** Mirror of `screens.Gewerk.android`. */
@Serializable
data class GewerkScreenText(val tabs: List<SelectableTabText>)

@Serializable
data class FilterChipText(val text: String, val selected: Boolean)

@Serializable
data class DocumentBannerText(val title: String, val sub: String, val amount: String, val documentId: String)

/** Mirror of `screens.Dokumente.android`. */
@Serializable
data class DokumenteScreenText(
    val filters: List<FilterChipText>,
    val searchLabel: String,
    val banner: DocumentBannerText,
    val recentLabel: String,
    val byTradeLabel: String,
    val tabs: List<TabText>,
    val countBadge: String,
)

/** Mirror of `screens.DokViewer.android`, `screens.Vergleich.android` and `screens.Mitteilungen.android`: no keys. */
@Serializable
class EmptyScreenText

/**
 * Decodes `screens.<name>.android` with the strict [FixtureJson].
 * A missing screen or `android` member throws [RepositoryException.FixtureMissing], a shape error [RepositoryException.Decoding].
 */
suspend fun <T> BackendRepository.screenText(name: String, serializer: KSerializer<T>): T {
    val member = (screen(name) as? JsonObject)?.get("android")
    if (member == null || member is JsonNull) throw RepositoryException.FixtureMissing("screens.$name.android")
    try {
        return FixtureJson.decodeFromJsonElement(serializer, member)
    } catch (e: SerializationException) {
        throw RepositoryException.Decoding(e.message ?: e.toString())
    } catch (e: IllegalArgumentException) {
        throw RepositoryException.Decoding(e.message ?: e.toString())
    }
}
