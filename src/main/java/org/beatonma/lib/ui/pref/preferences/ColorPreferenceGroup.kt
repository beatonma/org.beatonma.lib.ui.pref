package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.annotations.SerializedName
import org.json.JSONException
import org.json.JSONObject

class ColorPreferenceGroup @Throws(JSONException::class)
constructor(context: Context,
            obj: JSONObject
) : SimplePreference(context, obj), PreferenceContainer {

    companion object {
        private const val TAG = "ColorPreference"

        const val TYPE = "color_group"
        const val CHILD_COLORS = "colors"
    }

    @SerializedName(CHILD_COLORS)
    val colors: MutableList<ColorPreference> = mutableListOf()
    val keyMap = mutableMapOf<String, Int>()

    init {
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
            for (c in colors) {
                c.prefs = value
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

    override fun notifyUpdate(key: String, value: Int): Int {
        if (key in keyMap) {
            val position = keyMap[key]!!
            if (colors[position].update(value)) {
                return position
            }
        }
        return -1
    }

    override fun notifyUpdate(key: String, value: String?): Int {
        if (key in keyMap) {
            val position = keyMap[key]!!
            if (colors[position].update(value)) {
                return position
            }
        }
        return -1
    }

    override fun notifyUpdate(key: String, value: Boolean): Int {
        if (key in keyMap) {
            val position = keyMap[key]!!
            if (colors[position].update(value)) {
                return position
            }
        }
        return -1
    }

    override fun notifyUpdate(key: String, obj: Any?): Int {
        if (key in keyMap) {
            val position = keyMap[key]!!
            if (colors[position].update(obj)) {
                return position
            }
        }
        return -1
    }
}