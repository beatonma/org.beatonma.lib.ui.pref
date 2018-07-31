package org.beatonma.lib.ui.pref

import org.beatonma.lib.ui.pref.preferences.DependencyTests
import org.beatonma.lib.ui.pref.preferences.ResTest
import org.beatonma.lib.ui.pref.preferences.SharedPreferencesTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        DependencyTests::class,
        ResTest::class,
        SharedPreferencesTestSuite::class
)
class PackageUnitTestSuite
