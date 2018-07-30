@file:JvmName("SharedPreferencesTest")

package org.beatonma.lib.ui.pref.preferences

import android.content.SharedPreferences
import androidx.test.filters.SmallTest
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertEquals
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertTrue
import org.beatonma.lib.testing.kotlin.extensions.mock
import org.beatonma.lib.testing.kotlin.extensions.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.mockito.Matchers.anyBoolean
import org.mockito.Matchers.anyInt
import org.mockito.Matchers.anyString

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


/**
 * Ensure that saving a [preference][BasePreference] updates the corresponding
 * [SharedPreferences] value
 * TODO
 */
@SmallTest
class SaveSharedPreferencesTest {
    private val fakePrefs = HashMap<String, Any?>()

    private val editor = mock<SharedPreferences.Editor>().apply {
        whenever(putBoolean(anyString(), anyBoolean())).then {
            fakePrefs.put(it.arguments[0] as String, it.arguments[1])
        }
        whenever(putString(anyString(), anyString())).then {
            fakePrefs.put(it.arguments[0] as String, it.arguments[1])
        }
        whenever(putInt(anyString(), anyInt())).then {
            fakePrefs.put(it.arguments[0] as String, it.arguments[1])
        }
    }

    @Before
    fun setUp() {
        fakePrefs.clear()
    }

    @Test
    fun boolean_isUpdated_updatesSharedPreference() {
        val booleanPreference = BooleanPreference().apply {
            key = VALID_KEY
            isChecked = BOOLEAN_VALUE
        }

        booleanPreference.save(editor)

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
}

/**
 * Ensure that any existing [SharedPreferences] values are loaded to the
 * [preference][BasePreference].
 * TODO
 */
@SmallTest
class LoadSharedPreferenceTest {
    private val sharedPreferences = mock<SharedPreferences>().apply {
        whenever(getBoolean(anyString(), anyBoolean())).then { BOOLEAN_VALUE }
        whenever(getInt(anyString(), anyInt())).then { INT_VALUE }
        whenever(getString(anyString(), anyString())).then { STRING_VALUE }
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


