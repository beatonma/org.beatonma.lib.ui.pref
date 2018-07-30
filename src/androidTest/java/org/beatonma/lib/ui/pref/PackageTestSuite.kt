@file:JvmName("PackageTest")

package org.beatonma.lib.ui.pref

import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivityTest
import org.beatonma.lib.ui.pref.list.ListPreferenceActivityTestSuite
import org.beatonma.lib.ui.pref.preferences.ParcelableTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for all tests in this package
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
        PackageMediumTestSuite::class
)
class PackageTestSuite

/**
 * Test suite for all @MediumTest classes in this package
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
        ListPreferenceActivityTestSuite::class,
        ParcelableTest::class,
        SwatchColorPreferenceActivityTest::class
)
class PackageMediumTestSuite
