package de.baupilot.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoleResolutionTest {
    @Test
    fun picksTheFirstKnownRoleInPriorityOrder() {
        assertEquals("bauherr", resolveRole(listOf("handwerker", "bauherr", "architekt")))
        assertEquals("architekt", resolveRole(listOf("handwerker", "architekt")))
        assertEquals("handwerker", resolveRole(listOf("other", "handwerker")))
    }

    @Test
    fun noKnownTeamIsNull() {
        assertNull(resolveRole(emptyList()))
        assertNull(resolveRole(listOf("other", "Bauherr")))
    }
}
