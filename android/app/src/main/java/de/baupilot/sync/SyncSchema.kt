package de.baupilot.sync

enum class SyncTable(val id: String) {
    Projects("projects"),
    Members("members"),
    Trades("trades"),
    Offers("offers"),
    Documents("documents"),
    Inspections("inspections"),
    Steps("steps"),
    Defects("defects"),
    Signatures("signatures"),
    Notifications("notifications"),
    Photos("photos"),
    Protocols("protocols"),
}

enum class SyncBucket(val id: String) {
    Photos("photos"),
    Pdfs("pdfs"),
}

/** Remote column type. Sizes are the minimum the backend column must offer. */
sealed interface ColumnKind {
    data class Text(val size: Int) : ColumnKind
    data class Json(val size: Int) : ColumnKind
    data object Long_ : ColumnKind
    data object Double_ : ColumnKind
    data object Bool : ColumnKind
    data class LongText(val maxBytes: Int) : ColumnKind
}

/**
 * Columns of one remote table. `fixtureFields` are exactly the serial names of the record DTO;
 * `extraFields` are sync-only columns that never reach a fixture decoder (invariant I7).
 */
data class TableSpec(
    val table: SyncTable,
    val fixtureFields: Map<String, ColumnKind>,
    val extraFields: Map<String, ColumnKind>,
)

object SyncSchema {
    const val DATABASE_ID = "baupilot"

    /** Columns present on every remote row. */
    val commonColumns: Map<String, ColumnKind> = linkedMapOf(
        "projectId" to ColumnKind.Text(64),
        "sortKey" to ColumnKind.Long_,
        "stamps" to ColumnKind.LongText(1_000_000),
        "deleted" to ColumnKind.Bool,
    )

    private val ID = ColumnKind.Text(64)
    private val TIMESTAMP = ColumnKind.Text(40)
    private val LONG_TEXT = ColumnKind.LongText(1_000_000)

    val tables: Map<SyncTable, TableSpec> = listOf(
        TableSpec(
            SyncTable.Projects,
            fixtureFields = linkedMapOf(
                "id" to ID,
                "buildingType" to ColumnKind.Text(64),
                "address" to ColumnKind.Text(255),
                "constructionStart" to ColumnKind.Text(32),
                "moveIn" to ColumnKind.Text(32),
                "phase" to ColumnKind.Json(512),
                "unreadNotifications" to ColumnKind.Long_,
                "owner" to ColumnKind.Json(1024),
            ),
            extraFields = emptyMap(),
        ),
        TableSpec(
            SyncTable.Members,
            fixtureFields = linkedMapOf(
                "userId" to ID,
                "role" to ColumnKind.Text(16),
            ),
            extraFields = emptyMap(),
        ),
        TableSpec(
            SyncTable.Trades,
            fixtureFields = linkedMapOf(
                "id" to ID,
                "name" to ColumnKind.Text(128),
                "phase" to ColumnKind.Long_,
                "window" to ColumnKind.Json(128),
                "scheduleState" to ColumnKind.Text(32),
                "askNow" to ColumnKind.Bool,
                "weekLabel" to ColumnKind.Text(64),
                "statusLabel" to ColumnKind.Text(128),
                "detail" to LONG_TEXT,
            ),
            extraFields = emptyMap(),
        ),
        TableSpec(
            SyncTable.Offers,
            fixtureFields = linkedMapOf(
                "id" to ID,
                "tradeId" to ID,
                "firm" to ColumnKind.Text(128),
                "price" to ColumnKind.Text(64),
                "best" to ColumnKind.Bool,
                "pending" to ColumnKind.Bool,
                "meta" to ColumnKind.Json(512),
                "documentId" to ID,
            ),
            extraFields = emptyMap(),
        ),
        TableSpec(
            SyncTable.Documents,
            fixtureFields = linkedMapOf(
                "id" to ID,
                "kind" to ColumnKind.Text(32),
                "tradeId" to ID,
                "title" to ColumnKind.Text(255),
                "meta" to ColumnKind.Json(512),
                "amount" to ColumnKind.Text(64),
                "due" to ColumnKind.Text(64),
                "statusLabel" to ColumnKind.Text(128),
                "viewer" to LONG_TEXT,
            ),
            extraFields = linkedMapOf(
                "fileId" to ID,
                "fileSha256" to ColumnKind.Text(64),
                "sizeBytes" to ColumnKind.Long_,
                "origin" to ColumnKind.Text(16),
                "importedAt" to TIMESTAMP,
            ),
        ),
        TableSpec(
            SyncTable.Inspections,
            fixtureFields = linkedMapOf(
                "tradeId" to ID,
                "name" to ColumnKind.Text(128),
                "sub" to ColumnKind.Json(512),
                "status" to ColumnKind.Text(32),
                "route" to ColumnKind.Text(255),
            ),
            extraFields = linkedMapOf(
                "signedAt" to TIMESTAMP,
                "protocolId" to ID,
            ),
        ),
        TableSpec(
            SyncTable.Steps,
            fixtureFields = linkedMapOf(
                "stepKey" to ColumnKind.Text(128),
                "tradeId" to ID,
                "status" to ColumnKind.Text(16),
                "values" to ColumnKind.Json(2048),
                "capturedAt" to TIMESTAMP,
            ),
            extraFields = emptyMap(),
        ),
        TableSpec(
            SyncTable.Defects,
            fixtureFields = linkedMapOf(
                "id" to ID,
                "tradeId" to ID,
                "trade" to ColumnKind.Json(512),
                "title" to ColumnKind.Text(255),
                "deadline" to ColumnKind.Text(32),
                "deadlineLabel" to ColumnKind.Text(64),
                "dueLabel" to ColumnKind.Text(64),
                "urgency" to ColumnKind.Text(32),
                "meta" to ColumnKind.Text(255),
                "photos" to ColumnKind.Long_,
                "severity" to ColumnKind.Text(32),
                "description" to ColumnKind.Text(2048),
                "elementId" to ID,
                "responsible" to ColumnKind.Text(128),
                "notifyByEmail" to ColumnKind.Bool,
            ),
            extraFields = linkedMapOf(
                "stepKey" to ColumnKind.Text(128),
                "origin" to ColumnKind.Text(16),
            ),
        ),
        TableSpec(
            SyncTable.Signatures,
            fixtureFields = linkedMapOf(
                "inspectionId" to ID,
                "role" to ColumnKind.Text(16),
                "name" to ColumnKind.Text(128),
                "strokes" to LONG_TEXT,
                "signedAt" to TIMESTAMP,
            ),
            extraFields = emptyMap(),
        ),
        TableSpec(
            SyncTable.Notifications,
            fixtureFields = linkedMapOf(
                "id" to ID,
                "kind" to ColumnKind.Text(32),
                "title" to ColumnKind.Text(255),
                "text" to ColumnKind.Text(512),
                "when" to ColumnKind.Text(64),
                "unread" to ColumnKind.Bool,
                "target" to ColumnKind.Text(255),
            ),
            extraFields = linkedMapOf("group" to ColumnKind.Text(128)),
        ),
        TableSpec(
            SyncTable.Photos,
            fixtureFields = linkedMapOf(
                "ownerTable" to ColumnKind.Text(16),
                "ownerId" to ID,
                "fileId" to ID,
                "sha256" to ColumnKind.Text(64),
                "takenAt" to TIMESTAMP,
                "bytes" to ColumnKind.Long_,
            ),
            extraFields = emptyMap(),
        ),
        TableSpec(
            SyncTable.Protocols,
            fixtureFields = linkedMapOf(
                "inspectionId" to ID,
                "fileId" to ID,
                "fileSha256" to ColumnKind.Text(64),
                "contentSha256" to ColumnKind.Text(64),
                "contentJson" to LONG_TEXT,
                "createdAt" to TIMESTAMP,
            ),
            extraFields = emptyMap(),
        ),
    ).associateBy { it.table }
}
