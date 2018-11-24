@file:JvmName("SharedPreferencesTest")

package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.test.filters.SmallTest
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertEquals
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertTrue
import org.beatonma.lib.testing.kotlin.extensions.mock
import org.beatonma.lib.testing.mocks.mockedSharedPreferences
import org.beatonma.lib.testing.mocks.mockedSharedPreferencesEditor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

private const val VALID_KEY = "valid_key"
private const val UNKNOWN_KEY = "invalid_key"
private const val STRING_VALUE = "string_value"
private const val INT_VALUE = 31
private const val BOOLEAN_VALUE = true
private const val RESOURCE_ID_VALUE = 43

@RunWith(Suite::class)
@Suite
.SuiteClasses(
        SaveSharedPreferencesTest::class,
        LoadSharedPreferenceTest::class
)
class SharedPreferencesTestSuite

private val fakePrefs = HashMap<String, Any?>()
private val sharedPreferences = mockedSharedPreferences(
        intValue = INT_VALUE,
        booleanValue = BOOLEAN_VALUE,
        stringValue = STRING_VALUE
)
private val editor = mockedSharedPreferencesEditor(fakePrefs)

/**
 * Ensure that saving a [preference][BasePreference] updates the corresponding
 * [SharedPreferences] value
 * TODO
 */
@SmallTest
class SaveSharedPreferencesTest {

    @Before
    fun setUp() {
        fakePrefs.clear()
    }

    @Test
    fun appList_isUpdated_updatesSharedPreference() {
        val appListPreference = AppListPreference().apply {
            key = VALID_KEY
            selectedAppName = STRING_VALUE
            selectedAppPackage = STRING_VALUE
            selectedAppActivity = STRING_VALUE
            save(editor)
        }

        fakePrefs[VALID_KEY].assertEquals(STRING_VALUE)
        fakePrefs[appListPreference.keyNiceName].assertEquals(STRING_VALUE)
        fakePrefs[appListPreference.keyPackage].assertEquals(STRING_VALUE)
    }

    @Test
    fun boolean_isUpdated_updatesSharedPreference() {
        BooleanPreference().apply {
            key = VALID_KEY
            isChecked = BOOLEAN_VALUE
            save(editor)
        }

        fakePrefs[VALID_KEY].assertEquals(BOOLEAN_VALUE)
    }

    @Test
    fun color_isUpdated_updatesSharedPreference() {
        val testColor = ColorItem(0xFF_FF_00_FF.toInt(), 3, 7)
        ColorPreference().apply {
            key = VALID_KEY
            color.color = testColor.color
            color.swatch = testColor.swatch
            color.swatchPosition = testColor.swatchPosition
            save(editor)
        }

        fakePrefs[VALID_KEY].assertEquals(testColor.color)
        fakePrefs[swatchKey(VALID_KEY)].assertEquals(testColor.swatch)
        fakePrefs[swatchPositionKey(VALID_KEY)].assertEquals(testColor.swatchPosition)
    }

    @Test
    fun list_isUpdated_updatesSharedPreference() {
        val listPreference = ListPreference().apply {
            key = VALID_KEY
            selectedValue = INT_VALUE
            selectedDisplay = STRING_VALUE
            save(editor)
        }

        fakePrefs[VALID_KEY].assertEquals(INT_VALUE)
        fakePrefs[listPreference.displayKey].assertEquals(STRING_VALUE)
    }

    @Test
    fun preferenceGroup_preInit_savesDefaultValuesToSharedPreferences() {
        // TODO Mock JSONObject or move to androidTest
        val context = mock<Context>()
        val definitions = """
            {
                "type": "group",
                "prefs": "test_prefs",
                "items": [
                    {
                        "name": "boolean pref",
                        "key": "bool"
                        "type": "bool_switch",
                        "checked": true
                    },
                    {
                        "name": "color pref",
                        "key": "color",
                        "type": "color",
                        "color": "#ff00ff"
                    }
                ]
            }
        """.trimIndent()

        val group = buildPreferencesFromJson(context, definitions)
        group.preInit(sharedPreferences)

        fakePrefs["bool"].assertEquals(true)
        fakePrefs["color"].assertEquals(0xFF_FF_00_FF.toInt())
    }
}

/**
 * Ensure that any existing [SharedPreferences] values are loaded to the
 * [preference][BasePreference].
 * TODO
 */
@SmallTest
class LoadSharedPreferenceTest {
    @Test
    fun appList_load_loadsValueFromSharedPreference() {
        AppListPreference().apply {
            key = VALID_KEY
            load(sharedPreferences)

            selectedAppName.assertEquals(STRING_VALUE)
            selectedAppPackage.assertEquals(STRING_VALUE)
            selectedAppActivity.assertEquals(STRING_VALUE)
        }
    }

    @Test
    fun boolean_load_loadsValueFromSharedPreferences() {
        BooleanPreference().apply {
            key = VALID_KEY
            load(sharedPreferences)

            isChecked.assertTrue()
        }
    }

    @Test
    fun color_load_loadsValueFromSharedPreferences() {
        ColorPreference().apply {
            key = VALID_KEY
            load(sharedPreferences)

            color.color.assertEquals(INT_VALUE)
            color.swatch.assertEquals(INT_VALUE)
            color.swatchPosition.assertEquals(INT_VALUE)
        }
    }

    @Test
    fun list_load_loadsValueFromSharedPreferences() {
        ListPreference().apply {
            key = VALID_KEY
            load(sharedPreferences)

            selectedValue.assertEquals(INT_VALUE)
            selectedDisplay.assertEquals(STRING_VALUE)
        }
    }
}


