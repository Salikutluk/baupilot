package de.baupilot.protocol

import kotlinx.serialization.Serializable

/** Signature strokes in a `w` x `h` space. Each stroke is a flat list `x0, y0, x1, y1, ...`; the origin is top left. */
@Serializable
data class StrokesDto(val w: Double, val h: Double, val strokes: List<List<Double>>)

@Serializable
data class SignatureBlock(
    val role: String,
    val name: String,
    val label: String,
    val strokes: StrokesDto?,
    val signedAt: String?,
)

@Serializable
data class ProtocolDocument(
    val protocolId: String,
    val projectId: String,
    val address: String,
    val tradeId: String,
    val tradeName: String,
    val title: String,
    val stats: List<String>,
    val defects: List<String>,
    val reservation: String?,
    val signatures: List<SignatureBlock>,
    /** ISO-8601 instant. */
    val createdAt: String,
    /** Europe/Berlin, `dd.MM.yyyy HH:mm`. */
    val createdLocal: String,
)
