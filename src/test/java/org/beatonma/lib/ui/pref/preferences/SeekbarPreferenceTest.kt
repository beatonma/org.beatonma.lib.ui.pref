package org.beatonma.lib.ui.pref.preferences

import androidx.test.filters.SmallTest
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertEquals
import org.beatonma.lib.testing.mocks.mockedSharedPreferences
import org.beatonma.lib.testing.mocks.mockedSharedPreferencesEditor
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

internal const val KEY = "some_key"

@RunWith(Suite::class)
@Suite.SuiteClasses(
        IntSeekbarParamsTest::class,
        FloatSeekbarParamsTest::class
)
class SeekbarUnitTestSuite

@SmallTest
class IntSeekbarParamsTest {
    @Test
    fun params_stepCount_isCorrect() {
        IntSeekbarParams(KEY, min = 0, max = 10, stepSize = 1).stepCount.assertEquals(11)
        IntSeekbarParams(KEY, min = 0, max = 10, stepSize = 2).stepCount.assertEquals(6)
        IntSeekbarParams(KEY, min = 0, max = 9, stepSize = 3).stepCount.assertEquals(4)
    }

    @Test
    fun params_valueToSeekbarStep_isCorrect() {
        val params = IntSeekbarParams(KEY, min = 3, max = 10)

        params.run {
            // Minimum valid value
            valueToSeekbarStep(3).assertEquals(0)

            // Normal in-range values
            valueToSeekbarStep(5).assertEquals(2)
            valueToSeekbarStep(8).assertEquals(5)

            // Maximum valid value
            valueToSeekbarStep(10).assertEquals(7)

            // Invalid value (lower than min)
            valueToSeekbarStep(0).assertEquals(0)

            // Invalid value (higher than max)
            valueToSeekbarStep(11).assertEquals(7)
        }
    }

    @Test
    fun params_withDefaultInitiationValues_hasCorrectValues() {
        val params = IntSeekbarParams(KEY, max = 10)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(11, "stepCount")
            value.assertEquals(0, "value")
        }
    }

    @Test
    fun params_withPositiveMinimum_hasCorrectValues() {
        val params = IntSeekbarParams(KEY, min = 2, max = 10)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(9, "stepCount")
            value.assertEquals(2, "value")
        }
    }

    @Test
    fun params_withNegativeMinimum_hasCorrectValues() {
        val params = IntSeekbarParams(KEY, min = -2, max = 10)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(13, "stepCount")
            value.assertEquals(-2, "value")
        }
    }

    @Test
    fun params_withNegativeMinimumAndNegativeMaximum_hasCorrectValues() {
        val params = IntSeekbarParams(KEY, min = -10, max = -1)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(10, "stepCount")
            value.assertEquals(-10, "value")
        }
    }

    @Test
    fun params_withSelectedStep_hasCorrectValues() {
        val params = IntSeekbarParams(KEY, min = -10, max = 10)

        params.run {
            selectedStep = 3

            stepCount.assertEquals(21, "stepCount")
            value.assertEquals(-7)
        }
    }

    @Test
    fun params_saveAndLoad_areCorrect() {
        val fakePrefs = HashMap<String, Any?>()
        val mockEditor = mockedSharedPreferencesEditor(fakePrefs)

        val oldParams = IntSeekbarParams(KEY, min = -10, max = 10).apply {
            selectedStep = valueToSeekbarStep(5)
        }
        oldParams.save(mockEditor)
        fakePrefs[seekbarValueKey(KEY)].assertEquals(5)

        val mockPrefs = mockedSharedPreferences(intValue = 6)
        val newParams = IntSeekbarParams(KEY, max = 10)
        newParams.load(mockPrefs)
        newParams.value.assertEquals(6)
    }
}

@SmallTest
class FloatSeekbarParamsTest {
    @Test
    fun params_stepCount_isCorrect() {
        FloatSeekbarParams(KEY, min = 0F, max = 1F, stepSize = 1F).stepCount.assertEquals(2)
        FloatSeekbarParams(KEY, min = 0F, max = 1F, stepSize = .5F).stepCount.assertEquals(3)
        FloatSeekbarParams(KEY, min = 0F, max = 4.5F, stepSize = .25F).stepCount.assertEquals(19)
        FloatSeekbarParams(KEY, min = 3F, max = 7.5F, stepSize = .5F).stepCount.assertEquals(10)
    }

    @Test
    fun params_valueToSeekbarStep_isCorrect() {
        val params = FloatSeekbarParams(KEY, min = 3F, max = 7.5F, stepSize = .5F)

        params.run {
            // Minimum valid value
            valueToSeekbarStep(3F).assertEquals(0)

            // Normal in-range values
            valueToSeekbarStep(5F).assertEquals(4)
            valueToSeekbarStep(6.5F).assertEquals(7)

            // Maximum valid value
            valueToSeekbarStep(7.5F).assertEquals(9)

            // Invalid value (lower than min)
            valueToSeekbarStep(0F).assertEquals(0)

            // Invalid value (higher than max)
            valueToSeekbarStep(11F).assertEquals(9)

            // Invalid value (between steps)
            valueToSeekbarStep(3.25F).assertEquals(0)
        }
    }

    @Test
    fun params_withDefaultInitiationValues_hasCorrectValues() {
        val params = FloatSeekbarParams(KEY, max = 10F)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(11, "stepCount")
            value.assertEquals(0F, "value")
        }
    }

    @Test
    fun params_withPositiveMinimum_hasCorrectValues() {
        val params = FloatSeekbarParams(KEY, min = 2F, max = 10F)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(9, "stepCount")
            value.assertEquals(2F, "value")
        }
    }

    @Test
    fun params_withNegativeMinimum_hasCorrectValues() {
        val params = FloatSeekbarParams(KEY, min = -2F, max = 10F)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(13, "stepCount")
            value.assertEquals(-2F, "value")
        }
    }

    @Test
    fun params_withNegativeMinimumAndNegativeMaximum_hasCorrectValues() {
        val params = FloatSeekbarParams(KEY, min = -10F, max = -1F)

        params.run {
            selectedStep.assertEquals(0, "selectedStep")
            stepCount.assertEquals(10, "stepCount")
            value.assertEquals(-10F, "value")
        }
    }

    @Test
    fun params_withSelectedStep_hasCorrectValues() {
        val params = FloatSeekbarParams(KEY, min = 0F, max = 1F, stepSize = .25F)

        params.run {
            selectedStep = 3

            stepCount.assertEquals(5, "stepCount") // TODO: FAILS
            value.assertEquals(0.75F, "value")
        }
    }

    @Test
    fun params_saveAndLoad_areCorrect() {
        val fakePrefs = HashMap<String, Any?>()
        val mockEditor = mockedSharedPreferencesEditor(fakePrefs)

        val oldParams = FloatSeekbarParams(KEY, min = -10F, max = 10F).apply {
            selectedStep = valueToSeekbarStep(5F)
        }
        oldParams.save(mockEditor)
        fakePrefs[seekbarValueKey(KEY)].assertEquals(5F)

        val mockPrefs = mockedSharedPreferences(floatValue = 6F)
        val newParams = FloatSeekbarParams(KEY, max = 10F)
        newParams.load(mockPrefs)
        newParams.value.assertEquals(6F)
    }
}
