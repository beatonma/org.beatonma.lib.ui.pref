package org.beatonma.lib.ui.pref.color

import androidx.test.filters.MediumTest
import org.beatonma.lib.testing.kotlin.extensions.ActivityTest
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertEquals
import org.beatonma.lib.testing.kotlin.extensions.testRule
import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivity.Companion.EXTRA_COLOR_PREFERENCE
import org.beatonma.lib.ui.pref.preferences.ColorPreference
import org.json.JSONObject
import org.junit.Test


private const val COLOR_PREFERENCE = """
{
  "name": "single color",
  "key": "single_color_test",
  "type": "color",
  "color": "#00ff00"
}
"""

@MediumTest
class SwatchColorPreferenceActivityTest: ActivityTest<SwatchColorPreferenceActivity>() {
    override val rule = SwatchColorPreferenceActivity::class.testRule {
        it.putExtra(EXTRA_COLOR_PREFERENCE, ColorPreference(targetContext, JSONObject(COLOR_PREFERENCE)))
    }

    internal val customColorFragment: CustomColorFragment?
        get() = activity?.supportFragmentManager?.findFragmentByTag(CustomColorFragment.TAG) as? CustomColorFragment
    internal val materialColorsFragment: MaterialColorsFragment?
        get() = activity?.supportFragmentManager?.findFragmentByTag(MaterialColorsFragment.TAG) as? MaterialColorsFragment

    internal val viewModel: ColorPreferenceViewModel?
        get() = activity?.viewModel

    @Test
    fun preferenceViewModel_isInitiatedCorrectly() {
        viewModel?.colorPreference?.value?.color?.color.assertEquals((0xFF_00_FF_00).toInt())
    }
}
