package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.graphics.Color
import org.beatonma.lib.util.kotlin.extensions.colorCompat
import java.util.regex.Pattern

/**
 * Utility methods for reading values from [Resources] using plaintext resource IDs
 * e.g. "@string/name"
 */

/**
 * If the text represents a @resource, return the value of that resource
 * else return the given text
 */
internal fun getString(
        context: Context,
        text: String?
): String? {
    val resId = getResourceId(context, text)
    return if (resId == 0) text else context.resources.getString(resId)
}

internal fun getBoolean(
        context: Context,
        text: String?
): Boolean {
    val resId = getResourceId(context, text)
    return if (resId == 0) text?.toBoolean() ?: false else context.resources.getBoolean(resId)
}


/**
 * If the text represents a @resource, return the value of that resource
 * else try to parse an integer value
 */
internal fun getInt(
        context: Context,
        text: String
): Int {
    val resId = getResourceId(context, text)

    return if (resId == 0) {
        try {
            Integer.valueOf(text)
        } catch (e: Exception) {
            0
        }
    } else {
        context.resources.getInteger(resId)
    }
}

internal fun getFloat(
        context: Context,
        text: String
): Float {
    val resId = getResourceId(context, text)

    return if (resId == 0) {
        try {
            text.toFloat()
        } catch (e: Exception) {
            0F
        }
    } else {
        context.resources.getFraction(resId, 1, 1)
    }
}

/**
 * If the text represents a @resource, return the value of that resource
 * else try to parse a color value. Supports integers or hex codes.
 */
internal fun getColor(
        context: Context,
        text: String
): Int {
    val resId = getResourceId(context, text)
    return if (resId == 0) {
        try {
            Color.parseColor(if (text.startsWith("#")) text else "#$text")
        } catch (e: Exception) {
            try {
                Integer.valueOf(text)
            } catch (e: Exception) {
                0
            }
        }
    } else {
        context.colorCompat(resId)
    }
}

internal fun getIntArray(
        context: Context,
        text: String
): IntArray? {
    val resId = getResourceId(context, text)
    return if (resId == 0) null else context.resources.getIntArray(resId)
}

internal fun getStringArray(
        context: Context,
        text: String
): Array<String>? {
    val resId = getResourceId(context, text)
    return if (resId == 0) null else context.resources.getStringArray(resId)
}

internal fun getResourceId(context: Context, key: String?): Int {
    val pattern = Pattern.compile("@(\\w+)/(.+)")
    val m = pattern.matcher(key)
    if (m.matches()) {
        val resType = m.group(1)
        val name = m.group(2)
        return context.resources.getIdentifier(
                name,
                resType,
                context.packageName)
    }
    return 0
}
