package de.baupilot.app

import de.baupilot.screens.ScreenCopy
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pins the only non-prototype text of M7 (D11): the two state words of the signature pad. */
class ScreenCopyTest {
    @Test
    fun signaturePadStateWords() {
        assertEquals("leer", ScreenCopy.Signature.STATE_EMPTY)
        assertEquals("unterschrieben", ScreenCopy.Signature.STATE_SIGNED)
    }
}
