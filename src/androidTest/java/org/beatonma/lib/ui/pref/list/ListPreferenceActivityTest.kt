package org.beatonma.lib.ui.pref.list

import androidx.recyclerview.widget.RecyclerView
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import org.beatonma.lib.testing.espresso.ViewHeightAtLeast
import org.beatonma.lib.testing.espresso.ViewHeightAtMost
import org.beatonma.lib.testing.espresso.ViewHeightIs
import org.beatonma.lib.testing.espresso.click
import org.beatonma.lib.testing.kotlin.extensions.ActivityTest
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertTrue
import org.beatonma.lib.testing.kotlin.extensions.testRule
import org.beatonma.lib.ui.pref.R
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity.Companion.EXTRA_LIST_PREFERENCE
import org.beatonma.lib.ui.pref.preferences.ListPreference
import org.hamcrest.Matchers.not
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.beatonma.lib.testing.R.string as TestStrings

private const val SHORT_LIST_PREFERENCE = """
{
  "name": "list pref",
  "description": "This is a list description",
  "key": "list_single_short",
  "type": "list_single",
  "items": "@array/string_array_example_short",
  "show_selected": true
}
"""

private const val LONG_LIST_PREFERENCE = """
{
  "name": "long list pref",
  "description": "This is a list description",
  "key": "list_single_long",
  "type": "list_single",
  "items": "@array/string_array_example_long",
  "show_selected": true
}
"""

@RunWith(Suite::class)
@Suite.SuiteClasses(
        SmallListPreferenceActivityTest::class,
        LargeListPreferenceActivityTest::class
)
class ListPreferenceActivityTestSuite

abstract class ListPreferenceActivityTest : ActivityTest<ListPreferenceActivity>() {
    internal val itemHeight: Int = targetResources.getDimensionPixelSize(R.dimen.item_height_single)
    internal val maxHeight: Int = targetResources.getDimensionPixelSize(R.dimen.popup_content_max_height)

    @Test
    fun clickOnItem_shouldClosePopup() {
        onView(withId(R.id.recyclerview))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        activity?.isFinishing?.assertTrue() // Also fine if activity is null
    }

    @Test
    fun clickOnItem_shouldSaveItemToPreferences() {
        TODO("Check that selecting an item correctly updates SharedPreferences")
    }

    @Test
    fun list_showsAtLeastThreeItems() {
        onView(withId(R.id.recyclerview))
                .check(matches(ViewHeightAtLeast(itemHeight * 3)))
    }

    @Test
    fun list_doesNotExceed_maxHeight() {
        onView(withId(R.id.recyclerview))
                .check(matches(ViewHeightAtMost(maxHeight)))
    }
}

@MediumTest
class SmallListPreferenceActivityTest : ListPreferenceActivityTest() {
    override val rule = ListPreferenceActivity::class.testRule {
        it.putExtra(EXTRA_LIST_PREFERENCE,
                ListPreference(
                        InstrumentationRegistry.getContext(),
                        JSONObject(SHORT_LIST_PREFERENCE)))
    }

    @Test
    fun withSmallList_recyclerViewIsCorrectSize() {
        onView(withId(R.id.recyclerview))
                .check(matches(ViewHeightIs(itemHeight * 3)))
    }

    @Test
    fun withSmallList_recyclerViewShowsAllItems() {
        onView(withId(R.id.recyclerview))
                .check(matches(isCompletelyDisplayed()))
    }
}

@MediumTest
class LargeListPreferenceActivityTest : ListPreferenceActivityTest() {
    override val rule = ListPreferenceActivity::class.testRule {
        it.putExtra(EXTRA_LIST_PREFERENCE,
                ListPreference(
                        InstrumentationRegistry.getContext(),
                        JSONObject(LONG_LIST_PREFERENCE)))
    }

    @Test
    fun withLargeList_recyclerViewIsLimitedToMaxSize() {
        onView(withId(R.id.recyclerview))
                .check(matches(ViewHeightIs(maxHeight)))
    }

    @Test
    fun withLargeList_recyclerViewIsScrollable() {
        // Check first item is visible
        onView(withText(TestStrings.array_item_1))
                .check(matches(isCompletelyDisplayed()))

        // Check last item is NOT visible (or may be null)
        try {
            onView(withText(TestStrings.array_item_30))
                    .check(matches(not(isDisplayed())))
        } catch (e: NoMatchingViewException) {
        }

        // Scroll to end
        onView(withId(R.id.recyclerview))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(29))

        // Check first item is now NOT visible (or may be null)
        try {
            onView(withText(TestStrings.array_item_1))
                    .check(matches(not(isDisplayed())))
        } catch (e: NoMatchingViewException) {
        }

        // Check last item is now visible
        onView(withText(TestStrings.array_item_30))
                .check(matches(isCompletelyDisplayed()))
    }
}
