@file:JvmName("PreferenceTests")

package org.beatonma.lib.ui.pref.preferences

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.test.filters.MediumTest
import org.beatonma.lib.testing.catchAll
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertEquals
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.lang.reflect.InvocationTargetException

private const val TEST_KEY = "someKey"
private const val TEST_PREFS = "prefsName"
private const val TEST_NAME = "someName"
private const val TEST_DESCRIPTION = "someDescription"
private const val TEST_RESOURCE_ID = 29

private const val TEST_BOOLEAN_SELECTED_DESCRIPTION = "selectedDescription"
private const val TEST_BOOLEAN_UNSELECTED_DESCRIPTION = "unselectedDescription"

private const val TEST_LIST_SELECTED_VALUE = 17
private const val TEST_LIST_SELECTED_DISPLAY = "displayItem"

private const val TEST_APP_NAME = "BmaLib"
private const val TEST_PACKAGE_NAME = "org.beatonma.lib"
private const val TEST_ACTIVITY_NAME = "MainActivity"

private const val TEST_COLOR_COLORINT = 0xFF_FF_FF_00.toInt()
private const val TEST_COLOR_SWATCH = 3
private const val TEST_COLOR_SWATCH_POSITION = 7
private const val TEST_COLOR_ALPHA_ENABLED = true

val testColors: ArrayList<ColorPreference>
    get() = arrayListOf(
            testColorPreference(
                    0xFF_FF_00_00.toInt(), 1, 3, TEST_COLOR_ALPHA_ENABLED),
            testColorPreference(
                    0xFF_00_FF_00.toInt(), 5, 7, TEST_COLOR_ALPHA_ENABLED),
            testColorPreference(
                    0xFF_00_00_FF.toInt(), 11, 13, TEST_COLOR_ALPHA_ENABLED))


/**
 * TODO Mock Parcelable and move to test dir
 * For each BasePreference subclass, ensure that [parcelizing][Parcelable] does not cause data loss.
 */
@MediumTest
class ParcelableTest {
    @Test
    fun simplePreference_parcelize_isConsistent() {
        val original: SimplePreference = SimplePreference().applyBaseValues()
        val fromParcel: SimplePreference = original.createCopyViaParcel()

        fromParcel.run {
            assertBaseValues(original)

            sameObject(original).assertTrue()
            sameContents(original).assertTrue()
        }

        assertGetterEquivalency(original, fromParcel)
    }

    @Test
    fun sectionSeparator_parcelize_isConsistent() {
        val original = testSectionSeparator()
        val fromParcel: SectionSeparator = original.createCopyViaParcel()

        fromParcel.run {
            assertBaseValues(original)
        }

        assertGetterEquivalency(original, fromParcel)
    }

    @Test
    fun booleanPreference_parcelize_isConsistent() {
        val testIsChecked = true

        val original = testBooleanPreference(testIsChecked)
        val fromParcel: BooleanPreference = original.createCopyViaParcel()

        fromParcel.run {
            // assertBaseValues() // removed because description changes depending on check state

            key.assertEquals(TEST_KEY)
            prefs.assertEquals(TEST_PREFS)
            name.assertEquals(TEST_NAME)

            isChecked.assertEquals(testIsChecked)
            selectedDescription.assertEquals(TEST_BOOLEAN_SELECTED_DESCRIPTION)
            unselectedDescription.assertEquals(TEST_BOOLEAN_UNSELECTED_DESCRIPTION)
            description.assertEquals(TEST_BOOLEAN_SELECTED_DESCRIPTION)

            sameObject(original).assertTrue()
            sameContents(original).assertTrue()
        }

        assertGetterEquivalency(original, fromParcel)
    }

    @Test
    fun colorPreference_parcelize_isConsistent() {
        val original = testColorPreference()

        val fromParcel: ColorPreference = original.createCopyViaParcel()
        fromParcel.run {
            assertBaseValues(original)
            with(color) {
                color.assertEquals(TEST_COLOR_COLORINT)
                swatch.assertEquals(TEST_COLOR_SWATCH)
                swatchPosition.assertEquals(TEST_COLOR_SWATCH_POSITION)
            }
            alphaEnabled.assertEquals(TEST_COLOR_ALPHA_ENABLED)
        }

        assertGetterEquivalency(original, fromParcel)
    }

    @Test
    fun colorPreferenceGroup_parcelize_isConsistent() {
        val original = testColorPreferenceGroup()

        val fromParcel: ColorPreferenceGroup = original.createCopyViaParcel()
        fromParcel.run {
            assertBaseValues(original)
            alphaEnabled.assertEquals(TEST_COLOR_ALPHA_ENABLED)

            // Check that child ColorPreferences are restored correctly
            colors.size.assertEquals(testColors.size)
            colors.forEachIndexed { i, colorPref ->
                colorPref.assertBaseValues(testColors[i])
                colorPref.assertEquals(testColors[i])
                colorPref.alphaEnabled.assertEquals(TEST_COLOR_ALPHA_ENABLED)
            }
        }

        assertGetterEquivalency(original, fromParcel)
    }

    @Test
    fun listPreference_parcelize_isConsistent() {
        val original = testListPreference()

        val fromParcel: ListPreference = original.createCopyViaParcel()

        fromParcel.run {
            assertBaseValues(original)

            selectedValue.assertEquals(TEST_LIST_SELECTED_VALUE)
            selectedDisplay.assertEquals(TEST_LIST_SELECTED_DISPLAY)
            displayListResourceId.assertEquals(TEST_RESOURCE_ID)
        }

        assertGetterEquivalency(original, fromParcel)
    }

    @Test
    fun appListPreference_parcelize_isConsistent() {
        val original = testAppListPreference()

        val fromParcel: AppListPreference = original.createCopyViaParcel()
        fromParcel.apply {
            assertBaseValues(original)
            selectedAppName.assertEquals(TEST_APP_NAME)
            selectedAppPackage.assertEquals(TEST_PACKAGE_NAME)
            selectedAppActivity.assertEquals(TEST_ACTIVITY_NAME)
        }

        assertGetterEquivalency(original, fromParcel)
    }

    @Test
    fun preferenceGroup_parcelize_isConsistent() {
        val testChildPreferences = arrayListOf(
                testBooleanPreference(key = "boolean"),
                testListPreference(key = "list"),
                testAppListPreference(key = "applist"),
                testSectionSeparator(key = "section"),
                testColorPreference(key = "color"),
                testColorPreferenceGroup(key = "colorgroup"))

        val original: PreferenceGroup = PreferenceGroup().apply {
            applyBaseValues()
            preferences = testChildPreferences
            updateDependencies()
        }

        val fromParcel: PreferenceGroup = original.createCopyViaParcel()
        fromParcel.run {
            assertBaseValues(original)

            preferences.size.assertEquals(testChildPreferences.size)
            testChildPreferences.forEach {
                (it in preferences).assertTrue()
                (it.key in keyMap).assertTrue()
                preferences[keyMap[it.key] ?: 0].assertEquals(it)
            }
        }

        assertGetterEquivalency(original, fromParcel)
    }
}


/*
 * Functions to build standard versions of each Preference type.
 */
private fun testBooleanPreference(checked: Boolean = true, key: String = TEST_KEY) =
        BooleanPreference().apply {
            applyBaseValues(key = key)
            isChecked = checked
            selectedDescription = TEST_BOOLEAN_SELECTED_DESCRIPTION
            unselectedDescription = TEST_BOOLEAN_UNSELECTED_DESCRIPTION
        }

private fun testListPreference(key: String = TEST_KEY) =
        ListPreference().apply {
            applyBaseValues(key = key)

            selectedValue = TEST_LIST_SELECTED_VALUE
            selectedDisplay = TEST_LIST_SELECTED_DISPLAY
            displayListResourceId = TEST_RESOURCE_ID
        }

private fun testAppListPreference(key: String = TEST_KEY) =
        AppListPreference().apply {
            applyBaseValues(key = key)
            selectedAppName = TEST_APP_NAME
            selectedAppPackage = TEST_PACKAGE_NAME
            selectedAppActivity = TEST_ACTIVITY_NAME
        }

private fun testColorPreference(
        color: Int = TEST_COLOR_COLORINT,
        swatch: Int = TEST_COLOR_SWATCH,
        swatchPosition: Int = TEST_COLOR_SWATCH_POSITION,
        alphaEnabled: Boolean = TEST_COLOR_ALPHA_ENABLED,
        key: String = TEST_KEY
) = ColorPreference().apply {
    applyBaseValues(key = key)
    update(color, swatch, swatchPosition)
    this.alphaEnabled = alphaEnabled
}

private fun testColorPreferenceGroup(
        alphaEnabled: Boolean = TEST_COLOR_ALPHA_ENABLED,
        key: String = TEST_KEY
) = ColorPreferenceGroup().apply {
    applyBaseValues(key = key)
    this.alphaEnabled = alphaEnabled
    colors.addAll(testColors)
    updateKeymap()
}

private fun testSectionSeparator(key: String = TEST_KEY) =
        SectionSeparator().applyBaseValues(key = key)


/**
 * [Parcelize][Parcelable] the given object and return a new instance by unpacking the parcel
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Parcelable> T.createCopyViaParcel(): T {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, describeContents())
    parcel.setDataPosition(0)

    // Get CREATOR from static val
    val creator = this::class.java.declaredFields.find {
        it.name == "CREATOR"
    }?.get(null) as Parcelable.Creator<T>

    return creator.createFromParcel(parcel).apply {
        assertFalse(this@apply === this@createCopyViaParcel)
    }
}

/**
 * Set default values for the fields in [BasePreference] that are used by all subclasses
 * Value for [key] may be customised but make sure to give the same value to [assertBaseValues]!
 */
internal fun <T : BasePreference> T.applyBaseValues(key: String = TEST_KEY): T {
    this.key = key
    prefs = TEST_PREFS
    name = TEST_NAME
    description = TEST_DESCRIPTION
    return this
}

/**
 * Call on a reconstructed preference to ensure that all [BasePreference] fields are restored
 * successfully.
 * Value for [key] may be customised but it is the same as given to [applyBaseValues]!
 */
internal fun <T : BasePreference> T.assertBaseValues(originalObj: T?, key: String = TEST_KEY) {
    key.assertEquals(key)
    prefs.assertEquals(TEST_PREFS)
    name.assertEquals(TEST_NAME)
    description.assertEquals(TEST_DESCRIPTION)
    sameObject(originalObj).assertTrue()
    sameContents(originalObj).assertTrue()
}

/**
 * Naively compare the return values of the 'getter' methods of each object.
 *
 * Assert that the return value of every public method with no parameters is the same for both
 * objects. This should not be considered reliable - just a basic safety net to maybe catch
 * something you forgot to include while implementing your Parcelable class.
 */
private fun <T : Parcelable> assertGetterEquivalency(
        one: T, other: T, message: String? = null
) {
    val tag = "METHOD_EQUIV"
    one.javaClass.methods.forEach { method ->
        catchAll(
                tag = tag,
                message = "Unable to invoke method <${one.javaClass.simpleName}>.${method.name} [$message]",
                catch = arrayOf(
                        IllegalArgumentException::class,
                        InvocationTargetException::class
                )) {
            val a = method.invoke(one)
            val b = method.invoke(other)

            Log.v(tag, "Method: <${one.javaClass.simpleName}>.${method.name}() returns '$a' : '$b'")
            a.assertEquals(b)
        }
    }
}
