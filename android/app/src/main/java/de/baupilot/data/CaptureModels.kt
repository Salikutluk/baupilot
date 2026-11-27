package de.baupilot.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** One captured inspection step (`steps` table). `status` is `ok`, `defect` or null. */
@Serializable
data class StepRecord(
    val stepKey: String,
    val tradeId: String,
    val status: String? = null,
    val values: JsonObject,
    val capturedAt: String,
)

/** One signature of an inspection (`signatures` table). `strokes` is `{w,h,strokes:[[x,y,…]]}` in dp. */
@Serializable
data class SignatureRecord(
    val inspectionId: String,
    val role: String,
    val name: String,
    val strokes: JsonObject,
    val signedAt: String,
)

/** One photo file reference (`photos` table). `ownerTable` is `steps` or `defects`. */
@Serializable
data class PhotoRecord(
    val ownerTable: String,
    val ownerId: String,
    val fileId: String,
    val sha256: String,
    val takenAt: String,
    val bytes: Long,
)

/** One generated protocol PDF (`protocols` table). */
@Serializable
data class ProtocolRecord(
    val inspectionId: String,
    val fileId: String,
    val fileSha256: String,
    val contentSha256: String,
    val contentJson: String,
    val createdAt: String,
)

/** One project member and their team role (`members` table). */
@Serializable
data class MemberRecord(
    val userId: String,
    val role: String,
)
