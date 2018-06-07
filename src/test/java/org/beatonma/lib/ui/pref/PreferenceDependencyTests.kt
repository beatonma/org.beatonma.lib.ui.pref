package org.beatonma.lib.ui.pref

import org.beatonma.lib.ui.pref.preferences.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Tests for preference {@link Dependency} relationships
 */
class PreferenceDependencyTests {
    @Test
    fun parseDependencyTest() {
        // Test simple case with == operator
        assertEquals(
                Dependency(key = "bool_switch",
                        operator = "==",
                        value = "true"),
                parseDependency("bool_switch==true"))

        // Test spacing around operator and name with number and != operator
        assertEquals(
                Dependency(key = "bool2_switch",
                        operator = "!=",
                        value = "false"),
                parseDependency("bool2_switch !=  false"))

        // Test numerical value and > operator
        assertEquals(
                Dependency(key = "a_number",
                        operator = ">",
                        value = "3"),
                parseDependency("a_number > 3"))

        // Test < operator and negative number value
        assertEquals(
                Dependency(key = "a_negative_number",
                        operator = "<",
                        value = "-1"),
                parseDependency("a_negative_number < -1"))

        assertNotEquals(
                Dependency(key = "a_negative_number",
                        operator = "<",
                        value = "-1"),
                parseDependency("a_false_negative_number < 1"))

        // Test return null when unable to parse stringip
        assertEquals(
                null,
                parseDependency("this is an invalid string"))
    }

    @Test
    fun booleanDependencyTest() {
        val dependantPref = SimplePreference().apply {
            key = "dependant"
            dependency = parseDependency("boolean_pref == true")
        }
        val booleanPref = BooleanPreference().apply {
            key = "boolean_pref"
            isChecked = true
        }

        assertEquals(true, booleanPref.meetsDependency(dependantPref))

        booleanPref.isChecked = false
        assertEquals(false, booleanPref.meetsDependency(dependantPref))
    }

    @Test
    fun listDependencyTest() {
        val dependantPref = SimplePreference().apply {
            key = "dependant"
            dependency = parseDependency("list_pref == 4")
        }
        val listPref = ListPreference().apply {
            key = "list_pref"
            selectedValue = 4
        }

        // Valid equal value selected
        assertEquals(true, listPref.meetsDependency(dependantPref))

        // Invalid equal value selected
        listPref.selectedValue = 5
        assertEquals(false, listPref.meetsDependency(dependantPref))

        // Selected value greater than > dependency value
        dependantPref.dependency = parseDependency("list_pref > 1")
        assertEquals(true, listPref.meetsDependency(dependantPref))

        // Selected value less than > dependency value
        listPref.selectedValue = 1
        assertEquals(false, listPref.meetsDependency(dependantPref))
    }
}