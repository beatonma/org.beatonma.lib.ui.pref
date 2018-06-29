package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import org.beatonma.lib.util.kotlin.extensions.clone
import org.json.JSONException
import org.json.JSONObject

class ColorPreferenceGroup : BasePreference, PreferenceContainer {

    companion object {
        private const val TAG = "ColorPreference"

        const val TYPE = "color_group"
        const val CHILD_COLORS = "colors"
    }

    val colors: MutableList<ColorPreference> = mutableListOf()
    val keyMap = mutableMapOf<String, Int>()

    constructor(source: ColorPreferenceGroup): super(source) {
        colors.clone(source.colors)
        keyMap.putAll(source.keyMap)
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        obj.optJSONArray(CHILD_COLORS).forEachObj {
            colors.add(ColorPreference(context, it))
            for (i in colors.indices) {
                keyMap[colors[i].key] = i
            }
        }
    }

    override var prefs: String? = null
        set(value) {
            super.prefs = value
            if (colors != null) { // super(source) calls this before colors is initiated
                for (c in colors) {
                    c.prefs = value
                }
            }
        }

    override val type: String
        get() = TYPE

    override fun load(preferences: SharedPreferences) {
        for (p in colors) {
            p.load(preferences)
        }
    }

    override fun save(editor: SharedPreferences.Editor) {
        for (p in colors) {
            p.save(editor)
        }
    }

    override fun load(context: Context) {
        val preferences = context.getSharedPreferences(
                name, Context.MODE_PRIVATE)
        load(preferences)
    }

    override fun save(context: Context) {
        val editor = context.getSharedPreferences(
                name, Context.MODE_PRIVATE).edit()
        save(editor)
        editor.apply()
    }

    override fun sameContents(other: Any?): Boolean {
        other as ColorPreferenceGroup
        return colors == other.colors
    }

    override fun notifyUpdate(pref: BasePreference): Int {
        val position = findKeyPosition(pref.key)
        if (position >= 0) {
            colors[position] = pref as ColorPreference
        }
        return position
    }

    override fun findKeyPosition(key: String): Int {
        return keyMap[key] ?: -1
    }

    override fun copyOf(): ColorPreferenceGroup {
        return ColorPreferenceGroup(this)
    }
}