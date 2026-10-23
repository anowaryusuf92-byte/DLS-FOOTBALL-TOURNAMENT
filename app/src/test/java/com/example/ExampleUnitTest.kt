package com.example

import com.example.data.model.SUPER_ADMIN_UID
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSuperAdminRoleValidation() {
        val superAdmin = UserProfile(
            uid = SUPER_ADMIN_UID,
            name = "Main Admin",
            role = "player" // Even if role field is player, UID grants Super Admin
        )
        assertTrue(superAdmin.isSuperAdmin())
        assertTrue(superAdmin.isAdmin())

        val regularPlayer = UserProfile(
            uid = "player_12345",
            name = "Player One",
            role = "player"
        )
        assertFalse(regularPlayer.isSuperAdmin())
        assertFalse(regularPlayer.isAdmin())

        val assignedAdmin = UserProfile(
            uid = "admin_9999",
            name = "Tournament Organizer",
            role = "admin"
        )
        assertFalse(assignedAdmin.isSuperAdmin())
        assertTrue(assignedAdmin.isAdmin())
    }
}
