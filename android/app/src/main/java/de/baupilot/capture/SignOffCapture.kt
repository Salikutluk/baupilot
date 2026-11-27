package de.baupilot.capture

import de.baupilot.inspection.SignatureStrokes

/** The owner's captured sign-off: who signed and what they drew. */
data class SignOffCapture(val inspectionId: String, val signerName: String, val strokes: SignatureStrokes)
