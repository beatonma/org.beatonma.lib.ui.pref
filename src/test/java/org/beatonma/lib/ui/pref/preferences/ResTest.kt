package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.test.filters.SmallTest
import org.beatonma.lib.testing.kotlin.extensions.assertions.assertEquals
import org.beatonma.lib.testing.kotlin.extensions.mock
import org.beatonma.lib.testing.kotlin.extensions.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Matchers.anyInt
import org.mockito.Matchers.anyString
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

private const val VALID_COLOR_RESOURCE_VALUE = 13
private const val VALID_INTEGER_RESOURCE_VALUE = 17
private const val VALID_STRING_RESOURCE_VALUE = "resource_string"

private const val VALID_RESOURCE_ID = 11
private const val NOT_FOUND = 0

@RunWith(PowerMockRunner::class)
@PrepareForTest(Color::class)
@SmallTest
class ResTest {
    internal val context = mock<Context>()
    internal val resources = mock<Resources>()

    @Before
    fun setUp() {
        whenever(context.resources).then { resources }
        whenever(resources.getIdentifier(anyString(), anyString(), anyString()))
                .then { VALID_RESOURCE_ID }
        whenever(resources.getInteger(anyInt()))
                .then { VALID_INTEGER_RESOURCE_VALUE }
        whenever(resources.getString(anyInt()))
                .then { VALID_STRING_RESOURCE_VALUE }
        whenever(context.getColor(anyInt()))
                .then { VALID_COLOR_RESOURCE_VALUE }
        whenever(resources.getColor(anyInt()))
                .then { VALID_COLOR_RESOURCE_VALUE }
    }

    @Test
    fun getResourceId_parsesResourceIdCorrectly() {
        getResourceId(context, "@string/some_string").assertEquals(VALID_RESOURCE_ID)
        getResourceId(context, "@color/some_color").assertEquals(VALID_RESOURCE_ID)
        getResourceId(context, "@dimen/some_dimen").assertEquals(VALID_RESOURCE_ID)
        getResourceId(context, "@boolean/some_boolean").assertEquals(VALID_RESOURCE_ID)
    }

    @Test
    fun getResourceId_returnsZeroWhenResourceIdNotParsed() {
        getResourceId(context, "@@@@").assertEquals(NOT_FOUND)
        getResourceId(context, "@/").assertEquals(NOT_FOUND)
        getResourceId(context, "@a/").assertEquals(NOT_FOUND)
        getResourceId(context, "@/a").assertEquals(NOT_FOUND)
        getResourceId(context, "string/malformed_string_id").assertEquals(NOT_FOUND)
        getResourceId(context, "generic string").assertEquals(NOT_FOUND)
        getResourceId(context, "123").assertEquals(NOT_FOUND)
        getResourceId(context, "a@string/malformed_string_id").assertEquals(NOT_FOUND)
    }

    @Test
    fun getColor_withColorHexCode_returnsIntColorValue() {
        withMockColor {
            BDDMockito.given(Color.parseColor("#ff0000")).willReturn(0xFF_FF_00_00.toInt())

            getColor(context, "#ff0000").assertEquals(0xFF_FF_00_00.toInt())
            getColor(context, "ff0000").assertEquals(0xFF_FF_00_00.toInt())
        }
    }

    @Test
    fun getColor_withIntegerString_returnsIntColorValue() {
        withMockColor {
            BDDMockito.given(Color.parseColor(anyString())).willThrow(IllegalArgumentException())

            getColor(context, "-1").assertEquals(0xFF_FF_FF_FF.toInt())
            getColor(context, "-65536").assertEquals(0xFF_FF_00_00.toInt())
        }
    }

    @Test
    fun getColor_withColorResource_returnsResourceColorValue() {
        getColor(context, "@color/any_color").assertEquals(VALID_COLOR_RESOURCE_VALUE)
    }

    @Test
    fun getString_withStringResource_returnsResourceStringValue() {
        getString(context, "@string/any_string").assertEquals(VALID_STRING_RESOURCE_VALUE)
    }

    @Test
    fun getString_withStringLiteral_returnsStringLiteral() {
        val literalText = "literal text 123 blah... blah"
        getString(context, literalText).assertEquals(literalText)
    }
}


private inline fun withMockColor(block: () -> Unit) {
    PowerMockito.mockStatic(Color::class.java)

    block()

    PowerMockito.verifyStatic()
}
