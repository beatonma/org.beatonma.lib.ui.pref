package org.beatonma.lib.ui.pref.preferences

import androidx.test.filters.MediumTest
import org.beatonma.lib.testing.espresso.action.setSeekBarProgress
import org.beatonma.lib.testing.kotlin.extensions.ActivityTest
import org.beatonma.lib.testing.kotlin.extensions.ViewWithID
import org.beatonma.lib.testing.kotlin.extensions.testRule
import org.beatonma.lib.ui.pref.R
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

private val INT_SEEKBAR_PREFERENCE = """
    {
      "name": "integer seekbar",
      "key": "seekbar_int",
      "type": "seekbar_int",
      "min": -1,
      "max": 6,
      "value": 2
    }
""".wrapInPreferenceGroup()

private val FLOAT_SEEKBAR_PREFERENCE = """
    {
      "name": "float seekbar",
      "key": "seekbar_float",
      "type": "seekbar_float",
      "min": "0",
      "max": "3",
      "step_size": "0.25",
      "value": 1.75
    }
""".wrapInPreferenceGroup()

@RunWith(Suite::class)
@Suite.SuiteClasses(
        IntSeekbarPreferenceActivityTest::class,
        FloatSeekbarPreferenceActivityTest::class
)
class SeekbarPreferenceActivityTestSuite

/**
 * UI interaction helper class. Tests should use this (via robot {...}) - they should not touch
 * the UI directly.
 */
internal class SeekbarRobot {
    fun selectStep(step: Int) {
        ViewWithID(R.id.seekbar).perform { setSeekBarProgress(step) }
    }

    fun shouldShowValue(valueString: String) {
        ViewWithID(R.id.seekbar_value).hasText(valueString)
    }
}

abstract class SeekbarPreferenceActivityTest : ActivityTest<TestPreferenceActivity>() {
    internal fun robot(func: SeekbarRobot.() -> Any?) =  SeekbarRobot().apply {
        func()

        // Clear preferences after executing each robot {...} block
        rule.activity.clearPreferences()
    }
}

@MediumTest
class IntSeekbarPreferenceActivityTest : SeekbarPreferenceActivityTest() {
    override val rule = TestPreferenceActivity::class.testRule {
        it.putExtra(EXTRA_RAW_JSON, INT_SEEKBAR_PREFERENCE)
    }

    @Test
    fun seekbar_defaultValue_showsCorrectValue() {
        robot {
            shouldShowValue("2")
        }
    }

    @Test
    fun seekbar_minimum_showsCorrectValue() {
        robot {
            selectStep(0)
            shouldShowValue("-1")
        }
    }

    @Test
    fun seekbar_maximum_showsCorrectValue() {
        robot {
            selectStep(10)
            shouldShowValue("6")
        }
    }

    @Test
    fun saveAndLoad_withMinimumValue_restoresCorrectValue() {
        robot {
            shouldShowValue("2")
            selectStep(0)
            rule.relaunch()
            shouldShowValue("-1")
        }
    }

    @Test
    fun saveAndLoad_withMidrangeValue_restoresCorrectValue() {
        robot {
            shouldShowValue("2")
            selectStep(1)
            rule.relaunch()
            shouldShowValue("0")
        }
    }

    @Test
    fun saveAndLoad_withMaximumValue_restoresCorrectValue() {
        robot {
            shouldShowValue("2")
            selectStep(100)
            rule.relaunch()
            shouldShowValue("6")
        }
    }
}

@MediumTest
class FloatSeekbarPreferenceActivityTest : SeekbarPreferenceActivityTest() {
    override val rule = TestPreferenceActivity::class.testRule {
        it.putExtra(EXTRA_RAW_JSON, FLOAT_SEEKBAR_PREFERENCE)
    }

    @Test
    fun seekbar_defaultValue_isCorrect() {
        robot {
            shouldShowValue("1.75")
        }
    }

    @Test
    fun seekbar_minimum_isCorrect() {
        robot {
            selectStep(0)
            shouldShowValue("0.0")
        }
    }

    @Test
    fun seekbar_maximum_isCorrect() {
        robot {
            selectStep(100)
            shouldShowValue("3.0")
        }
    }

    @Test
    fun saveAndLoad_withMinimumValue_restoresCorrectValue() {
        robot {
            shouldShowValue("1.75")
            selectStep(0)
            rule.relaunch()
            shouldShowValue("0.0")
        }
    }

    @Test
    fun saveAndLoad_withMidrangeValue_restoresCorrectValue() {
        robot {
            shouldShowValue("1.75")
            selectStep(1)
            rule.relaunch()
            shouldShowValue("0.25")
        }
    }

    @Test
    fun saveAndLoad_withMaximumValue_restoresCorrectValue() {
        robot {
            shouldShowValue("1.75")
            selectStep(100)
            rule.relaunch()
            shouldShowValue("3.0")
        }
    }
}
